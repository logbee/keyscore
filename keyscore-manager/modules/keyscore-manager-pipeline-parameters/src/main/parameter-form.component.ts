import {
    ChangeDetectionStrategy, ChangeDetectorRef,
    Component, ComponentFactory, ComponentFactoryResolver, ElementRef,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output, Renderer, Renderer2,
    ViewChild,
    ViewContainerRef
} from "@angular/core";
import {ParameterComponentFactoryService} from "./service/parameter-component-factory.service";
import {
    Parameter,
    ParameterDescriptor,
    ParameterMap
} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";
import {ParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/ParameterComponent";
import {ParameterDescriptorJsonClass} from '@keyscore-manager-models/src/main/parameters/parameter.model'
import {ParameterGroupDescriptor} from '@keyscore-manager-models/src/main/parameters/group-parameter.model'
import {ParameterGroupComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/parameter-group/parameter-group.component";
import {ParameterErrorWrapperComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameter-error-wrapper.component";
import {ParameterRef, Ref} from "@keyscore-manager-models/src/main/common/Ref";

@Component({
    selector: 'parameter-form',
    template: `
        <ng-template #formContainer></ng-template>
    `,
    styleUrls: ['./parameter-form.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ParameterFormComponent implements OnDestroy {

    @Input() set config(conf: { id: string, parameters: ParameterMap }) {
        if (!this._conf || conf.id != this._conf.id) {
            this.createParameterComponents(conf.parameters);
        } else if (this._conf) {
            let parametersToAdd: ParameterMap = {parameters: {}};
            const _parametersKeys = Array.from(Object.keys(this._conf.parameters.parameters));
            Object.entries(conf.parameters.parameters).forEach(([ref, [parameter, descriptor]]) => {
                if (!_parametersKeys.includes(ref)) {
                    parametersToAdd.parameters[ref] = [parameter, descriptor];
                }
            });
            this.createParameterComponents(
                parametersToAdd,
                Object.keys(parametersToAdd.parameters).length === Object.keys(conf.parameters.parameters).length
            );
        }
        this._conf = conf;
        this.cd.markForCheck();
    };

    get config(): { id: string, parameters: ParameterMap } {
        return this._conf;
    }

    private _conf: { id: string, parameters: ParameterMap };

    @Input() autoCompleteDataList: string[] = [];

    @Input() set changedParameters(val: ParameterRef[]) {
        this._changedParameters = val;
        console.log("CHANGEDPARAMETERS ERRORWRAPPER: ", this._errorWrapperComponents);
        if (this._errorWrapperComponents.size) {
            Array.from(this._errorWrapperComponents.values()).forEach(wrapper =>
                wrapper.wasUpdated = this.changedParameters.map(ref => ref.id).includes(wrapper.descriptor.ref.id))
            console.log("CHANGEDPARAMETERS IN FORM SETTER: ", this.changedParameters)
        }
    }

    get changedParameters() {
        return this._changedParameters;
    }

    private _changedParameters: ParameterRef[] = [];

    @Output() onValueChange: EventEmitter<Parameter> = new EventEmitter();

    @ViewChild("formContainer", {read: ViewContainerRef}) formContainer: ViewContainerRef;

    private _unsubscribe$: Subject<void> = new Subject<void>();

    private _parameterComponents: Map<string, ParameterComponent<ParameterDescriptor, Parameter>> = new Map();
    private _errorWrapperComponents: Map<string, ParameterErrorWrapperComponent> = new Map();

    constructor(private componentFactoryResolver: ComponentFactoryResolver, private cd: ChangeDetectorRef, private renderer: Renderer2, private elem: ElementRef) {
    }


    createParameterComponents(parameters: ParameterMap, clearContainer: boolean = true) {
        console.log("UPDATEDPARAMETERS IN PARAMETER FORM: ", this.changedParameters);
        if (clearContainer) {
            this._unsubscribe$.next();
            this.formContainer.clear();
            this._parameterComponents = new Map();
            this._errorWrapperComponents = new Map();
        }
        if (parameters && parameters.parameters) {
            Object.entries(parameters.parameters).forEach(([ref, [parameter, descriptor]]) => {
                this.createParameterComponent(parameter, descriptor);
            })
        }
    }

    createParameterComponent(parameter: Parameter, descriptor: ParameterDescriptor) {
        if (descriptor.jsonClass === ParameterDescriptorJsonClass.ParameterGroupDescriptor) {
            this.connectGroupConditionToGroup(descriptor as ParameterGroupDescriptor);
        }

        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(ParameterErrorWrapperComponent);
        const componentRef = this.formContainer.createComponent(componentFactory);
        componentRef.instance.parameter = parameter;
        componentRef.instance.descriptor = descriptor;
        componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;
        componentRef.instance.wasUpdated =
            this.changedParameters ? this.changedParameters.map(ref => ref.id).includes(descriptor.ref.id) : false;

        componentRef.instance.onValueChange.pipe(takeUntil(this._unsubscribe$)).subscribe((parameter: Parameter) => {
                this.onValueChange.emit(parameter);
            }
        );
        componentRef.changeDetectorRef.detectChanges();
        this._parameterComponents.set(descriptor.ref.id, componentRef.instance.parameterComponent);
    }

    public triggerInputChangeDetection() {
        let inputs = this.elem.nativeElement.querySelectorAll('input');
        inputs.forEach(input => {
            input.blur();

        })
    }

    public confirmUpdatedParameters() {
        const updatedParameterWrappers: ParameterErrorWrapperComponent[] = this.getWrapperOfUpdatedParamters();
        updatedParameterWrappers.forEach(wrapper => wrapper.confirmUpdate())
    }


    private getWrapperOfUpdatedParamters() {
        return [...this._errorWrapperComponents].filter(([ref, _]) =>
            this.changedParameters.map(parameterRef => parameterRef.id).includes(ref)).map(([_, component]) => component);
    }

    private connectGroupConditionToGroup(descriptor: ParameterGroupDescriptor) {
        if (descriptor.condition) {
            setTimeout(() => {
                const groupComponent = this._parameterComponents.get(descriptor.ref.id);
                const conditionComponent = this._parameterComponents.get(descriptor.condition.parameter.id);
                if (!groupComponent || !conditionComponent) return;
                (groupComponent as ParameterGroupComponent).conditionInput = conditionComponent.value;
                conditionComponent.emitter.pipe(takeUntil(this._unsubscribe$)).subscribe((parameter: Parameter) => {
                    (groupComponent as ParameterGroupComponent).conditionInput = parameter;
                });
            }, 0)
        }
    }

    ngOnDestroy() {
        this._unsubscribe$.next();
        this._unsubscribe$.complete();
    }

}
