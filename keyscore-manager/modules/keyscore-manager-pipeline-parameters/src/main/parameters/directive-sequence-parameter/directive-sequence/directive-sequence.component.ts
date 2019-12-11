import {AfterViewInit, Component, Input, OnDestroy, ViewChild, ViewContainerRef} from "@angular/core";
import {
    FieldDirectiveSequenceParameterDescriptor,
    FieldDirectiveSequenceParameter,
    DirectiveConfiguration,
    FieldDirectiveSequenceConfiguration,
    FieldDirectiveDescriptor
} from '@keyscore-manager-models/src/main/parameters/directive.model';
import {ParameterDescriptor, Parameter} from '@keyscore-manager-models/src/main/parameters/parameter.model';
import {ParameterComponentFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-component-factory.service";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";

@Component({
    selector: 'ks-directive-sequence',
    template: `
        <mat-expansion-panel [hideToggle]="isDisabled()" [disabled]="isDisabled()">
            <mat-expansion-panel-header [collapsedHeight]="getExpansionHeight()"
                                        [expandedHeight]="getExpansionHeight()"
                                        fxLayout="row-reverse" fxLayoutGap="15px"
                                        fxLayoutAlign="space-between center">

                <div fxLayout="column" class="parameter-wrapper" (click)="$event.stopPropagation()">
                    <ng-template #parameterContainer></ng-template>
                </div>
                <button mat-button matSuffix mat-icon-button aria-label="delete directive sequence" fxFlexAlign="center"
                        (click)="delete($event)">
                    <mat-icon color="warn">delete</mat-icon>
                </button>
            </mat-expansion-panel-header>
            <div class="sequence-body" fxLayout="column" fxLayoutGap="8px">
                <mat-divider></mat-divider>

                <div cdkDropList class="directive-list" (cdkDropListDropped)="drop($event)" fxLayout="column" fxLayoutAlign="start stretch" fxLayoutGap="8px">
                    <ks-directive class="directive-draggable" *ngFor="let directive of sequence.directives" [configuration]="directive"
                                  [descriptor]="getDirectiveDescriptor(directive)" cdkDrag></ks-directive>
                </div>

            </div>
        </mat-expansion-panel>
    `,
    styleUrls: ['./directive-sequence.component.scss']
})
export class DirectiveSequenceComponent implements AfterViewInit, OnDestroy {

    @Input() sequence: FieldDirectiveSequenceConfiguration;
    @Input() descriptor: FieldDirectiveSequenceParameterDescriptor;
    @Input() autoCompleteDataList: string[];

    @ViewChild('parameterContainer', {read: ViewContainerRef}) parameterContainer: ViewContainerRef;

    private readonly PARAMETER_HEIGHT: number = 85;

    private _unsubscribe$: Subject<void> = new Subject<void>();


    constructor(private _parameterComponentFactory: ParameterComponentFactoryService) {

    }

    ngAfterViewInit(): void {
        this.createComponents();
    }

    private createComponents() {
        this.parameterContainer.clear();
        this.descriptor.parameters.forEach(parameterDescriptor => this.createComponent(parameterDescriptor));
    }

    private createComponent(descriptor: ParameterDescriptor) {
        const componentRef = this._parameterComponentFactory.createParameterComponent(descriptor.jsonClass, this.parameterContainer);
        componentRef.instance.descriptor = descriptor;
        componentRef.instance.parameter = this.sequence.parameters.parameters.find(param => param.ref.id === descriptor.ref.id);
        componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;

        componentRef.instance.emitter.pipe(takeUntil(this._unsubscribe$)).subscribe(parameter => this.onParameterChange(parameter))
    }

    private onParameterChange(parameter: Parameter) {
        console.log("Parameter Change!");
    }

    private getDirectiveDescriptor(config: DirectiveConfiguration) {
        const directiveDescriptor = this.descriptor.directives.find(descriptor => descriptor.ref.uuid === config.ref.uuid);
        if (!directiveDescriptor) {
            throw new Error(`[DirectiveSequenceComponent] No descriptor found for DirectiveConfiguration: ${config.ref.uuid}`);
        }
        return directiveDescriptor;
    }

    private isDisabled() {
        return this.sequence.directives.length === 0;
    }

    private delete(event: MouseEvent) {
        event.stopPropagation();
    }

    private getExpansionHeight() {
        const height = this.descriptor.parameters.length * this.PARAMETER_HEIGHT;
        return `${height}px`;
    }

    private drop(event: CdkDragDrop<DirectiveConfiguration[]>) {
        moveItemInArray(this.sequence.directives, event.previousIndex, event.currentIndex);
    }

    ngOnDestroy(): void {
        this._unsubscribe$.next();
        this._unsubscribe$.complete();
    }
}
