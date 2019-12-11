import {
    AfterViewInit,
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    Output,
    ViewChild,
    ViewContainerRef
} from "@angular/core";
import {
    FieldDirectiveDescriptor,
    DirectiveConfiguration
} from '@keyscore-manager-models/src/main/parameters/directive.model';
import {ParameterDescriptor, Parameter} from '@keyscore-manager-models/src/main/parameters/parameter.model';
import {ParameterComponentFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-component-factory.service";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";

@Component({
    selector: 'ks-directive',
    template: `
        <mat-expansion-panel [hideToggle]="descriptor.parameters.length === 0" [disabled]="descriptor.parameters.length === 0" class="directive-wrapper">
            <mat-expansion-panel-header class="directive-header" fxLayout="row-reverse"
                                        matTooltip="{{descriptor.description}}"
                                        fxLayoutAlign="space-between center" fxLayoutGap="15px">
                <mat-panel-title fxFlexAlign="center" class="directive-title">{{descriptor.displayName}}</mat-panel-title>
                <button mat-button matSuffix mat-icon-button aria-label="delete directive"
                        (click)="delete($event)">
                    <mat-icon color="warn">delete</mat-icon>
                </button>
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

    @ViewChild('parameterContainer', {read: ViewContainerRef}) parameterContainer: ViewContainerRef;

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

    delete(event:MouseEvent) {
        event.stopPropagation();
        this.onDelete.emit(this.configuration);
    }

    ngAfterViewInit(): void {
        this.createParameterComponents();
    }

    private createParameterComponents() {
        console.log("CONTAINER:", this.parameterContainer);
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

    ngOnDestroy(): void {
        this._unsubscribe$.next();
        this._unsubscribe$.complete();
    }

}
