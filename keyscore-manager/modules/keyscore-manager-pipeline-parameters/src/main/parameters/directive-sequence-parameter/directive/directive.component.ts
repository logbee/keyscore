import {
    AfterViewInit,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnDestroy,
    Output,
    ViewChild,
    ViewContainerRef
} from "@angular/core";
import {
    DirectiveConfiguration,
    FieldDirectiveDescriptor
} from '@keyscore-manager-models/src/main/parameters/directive.model';
import {Parameter, ParameterDescriptor} from '@keyscore-manager-models/src/main/parameters/parameter.model';
import {ParameterComponentFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-component-factory.service";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";
import {IconEncoding, IconFormat} from "@keyscore-manager-models/src/main/descriptors/Icon";

@Component({
    selector: 'ks-directive',
    template: `
        <mat-expansion-panel [hideToggle]="noParameters()"
                             [disabled]="noParameters()" class="directive-wrapper">
            <mat-expansion-panel-header class="directive-header" fxLayout="row-reverse"
                                        fxLayoutAlign="space-between center" fxLayoutGap="15px">
                <div fxLayout="row" fxLayoutAlign="space-between center" fxFlex>
                    <div *ngIf="noParameters()" fxFlexAlign="center">
                        <mat-icon></mat-icon>
                    </div>
                    <div *ngIf="!noParameters()" style="width:0px;height:20px"></div>
                    <div class="directive-title" fxLayout="row" fxLayoutGap="8px">
                        <div class="icon-wrapper" #iconContainer></div>
                        <span>{{descriptor.displayName}}</span>
                    </div>
                    <div>
                        <mat-icon></mat-icon>
                        <button mat-button matSuffix mat-icon-button aria-label="delete directive"
                                (click)="delete($event)">
                            <mat-icon color="warn">delete</mat-icon>
                        </button>
                    </div>
                </div>
            </mat-expansion-panel-header>

            <div fxLayout="column" fxLayoutGap="8px" class="directive-body">
                <mat-divider *ngIf="descriptor.parameters.length"></mat-divider>
                <ng-template #parameterContainer></ng-template>
            </div>
        </mat-expansion-panel>
    `,
    styleUrls: ['./directive.component.scss']
})
export class DirectiveComponent implements AfterViewInit, OnDestroy {
    @Input() descriptor: FieldDirectiveDescriptor;
    @Input() configuration: DirectiveConfiguration;
    @Output() onDelete: EventEmitter<DirectiveConfiguration> = new EventEmitter<DirectiveConfiguration>();
    @Output() onChange: EventEmitter<DirectiveConfiguration> = new EventEmitter<DirectiveConfiguration>();

    @ViewChild('parameterContainer', {read: ViewContainerRef, static: true}) parameterContainer: ViewContainerRef;
    @ViewChild('iconContainer', {static: true}) iconContainer: ElementRef;

    private _unsubscribe$: Subject<void> = new Subject<void>();

    constructor(private parameterComponentFactory: ParameterComponentFactoryService) {

    }

    change(parameter: Parameter) {
        const parameterIndex = this.configuration.parameters.parameters.findIndex(param =>
            param.ref.id === parameter.ref.id);

        if (parameterIndex > -1) {
            this.configuration.parameters.parameters.splice(parameterIndex, 1, parameter);
            this.onChange.emit(this.configuration);
        }
    }

    delete(event: MouseEvent) {
        event.stopPropagation();
        this.onDelete.emit(this.configuration);
    }

    ngAfterViewInit(): void {
        if (this.descriptor.icon && this.descriptor.icon.encoding === IconEncoding.RAW && this.descriptor.icon.format === IconFormat.SVG) {
            this.iconContainer.nativeElement.innerHTML = this.descriptor.icon.data;
        }
        this.createParameterComponents();

    }

    private createParameterComponents() {
        this.parameterContainer.clear();
        this.descriptor.parameters.forEach(descriptor =>
            this.createParameterComponent(descriptor, this.getParameter(descriptor))
        )
    }

    private createParameterComponent(descriptor: ParameterDescriptor, parameter: Parameter) {
        const componentRef =
            this.parameterComponentFactory.createParameterComponent(descriptor.jsonClass, this.parameterContainer);
        componentRef.instance.parameter = parameter;
        componentRef.instance.descriptor = descriptor;

        componentRef.instance.emitter.pipe(takeUntil(this._unsubscribe$)).subscribe(parameter => {
            this.change(parameter);
        })

    }

    private getParameter(descriptor: ParameterDescriptor): Parameter {
        const parameter = this.configuration.parameters.parameters.find(param => param.ref.id === descriptor.ref.id);
        if (!parameter) {
            throw new Error(`[DirectiveComponent] No matching Parameter was found for ParameterDescriptor: ${descriptor.displayName}`);
        }
        return parameter;
    }

    noParameters() {
        return this.descriptor.parameters && this.descriptor.parameters.length === 0;
    }

    ngOnDestroy(): void {
        this._unsubscribe$.next();
        this._unsubscribe$.complete();
    }
}
