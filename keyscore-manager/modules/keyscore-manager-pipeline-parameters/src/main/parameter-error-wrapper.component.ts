import {
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
    ViewChild,
    ViewContainerRef
} from "@angular/core";
import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {ParameterComponentFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-component-factory.service";
import {takeUntil} from "rxjs/operators";
import {Subject} from "rxjs";
import {ParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/ParameterComponent";

@Component({
    selector: 'ks-parameter-error-wrapper',
    template: `
        <div fxLayout="row" fxLayoutGap="8px" fxLayoutAlign="start center">
            <mat-icon *ngIf="wasUpdated" color="warn">warning</mat-icon>
            <ng-template #parameterContainer></ng-template>
        </div>
    `,
    styleUrls: ['./parameter-error-wrapper.component.scss']
})
export class ParameterErrorWrapperComponent implements OnInit, OnDestroy {

    @Input() parameter: Parameter;
    @Input() descriptor: ParameterDescriptor;
    @Input() autoCompleteDataList: string[];
    @Input() wasUpdated: boolean = false;

    @Output() onValueChange: EventEmitter<Parameter> = new EventEmitter<Parameter>();

    @ViewChild('parameterContainer', { read: ViewContainerRef, static: true }) parameterContainer: ViewContainerRef;

    parameterComponent: ParameterComponent<ParameterDescriptor, Parameter>;

    private _unsubscribe$: Subject<void> = new Subject<void>();

    constructor(private parameterComponentFactory: ParameterComponentFactoryService, private cd: ChangeDetectorRef) {

    }

    ngOnInit(): void {
        if (!this.parameter) {
            throw new Error(`[ParameterErrorWrapper] failed to initialise: no parameter was set`);
        }
        if (!this.descriptor) {
            throw new Error(`[ParameterErrorWrapper] failed to initialise: no descriptor was set`);
        }
        this.createParameterComponent(this.parameter, this.descriptor);

    }

    public confirmUpdate(isConfirmed: boolean) {
        this.wasUpdated = !isConfirmed;
        this.cd.detectChanges();
    }

    public isReadyToSave() {
        return !this.wasUpdated;
    }

    private createParameterComponent(parameter: Parameter, descriptor: ParameterDescriptor) {

        const componentRef = this.parameterComponentFactory.createParameterComponent(descriptor.jsonClass, this.parameterContainer);
        componentRef.instance.parameter = parameter;
        componentRef.instance.descriptor = descriptor;
        componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;

        componentRef.instance.emitter.pipe(takeUntil(this._unsubscribe$)).subscribe((parameter: Parameter) =>
            this.emitChanges(parameter)
        );

        this.parameterComponent = componentRef.instance;
    }


    private emitChanges(parameter: Parameter) {
        this.onValueChange.emit(parameter);
    }

    ngOnDestroy(): void {
        this._unsubscribe$.next();
        this._unsubscribe$.complete();
    }
}
