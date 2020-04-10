import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ComponentFactoryResolver,
    ElementRef,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
    Renderer2,
    ViewChild,
    ViewContainerRef
} from "@angular/core";
import {
    Parameter,
    ParameterDescriptor,
    ParameterDescriptorJsonClass,
    ParameterMap
} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {Observable, Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";
import {ParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/ParameterComponent";
import {ParameterGroupDescriptor} from '@keyscore-manager-models/src/main/parameters/group-parameter.model'
import {ParameterGroupComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/parameter-group/parameter-group.component";
import {ParameterErrorWrapperComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameter-error-wrapper.component";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";
import {PipelineConfigurationChecker} from "@/app/pipelines/services/pipeline-configuration-checker.service";

@Component({
    selector: 'parameter-form',
    template: `
        <ng-template #formContainer></ng-template>
    `,
    styleUrls: ['./parameter-form.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ParameterFormComponent implements OnInit, OnDestroy {

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

    private _changedParameters$: Observable<ParameterRef[]>;

    @Output() onValueChange: EventEmitter<Parameter> = new EventEmitter();

    @ViewChild("formContainer", { read: ViewContainerRef, static: true }) formContainer: ViewContainerRef;

    private _unsubscribe$: Subject<void> = new Subject<void>();

    private _parameterComponents: Map<string, ParameterComponent<ParameterDescriptor, Parameter>> = new Map();
    private _errorWrapperComponents: Map<string, ParameterErrorWrapperComponent> = new Map();

    constructor(private pipelineConfigurationChecker: PipelineConfigurationChecker, private componentFactoryResolver: ComponentFactoryResolver, private cd: ChangeDetectorRef, private renderer: Renderer2, private elem: ElementRef) {
    }

    ngOnInit(): void {
        this._changedParameters$ = this.pipelineConfigurationChecker.getUpdatedParametersForFilter(this.config.id);

        this._changedParameters$.pipe(takeUntil(this._unsubscribe$)).subscribe(parameters => {
            console.log("PARAMETRSCHANGEDINPARAMETERFORM", parameters);
            if (this._errorWrapperComponents.size) {
                Array.from(this._errorWrapperComponents.values()).forEach(wrapper =>
                    wrapper.confirmUpdate(!parameters.map(ref => ref.id).includes(wrapper.descriptor.ref.id)));
            }
        })
    }

    createParameterComponents(parameters: ParameterMap, clearContainer: boolean = true) {
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

        componentRef.instance.onValueChange.pipe(takeUntil(this._unsubscribe$)).subscribe((parameter: Parameter) => {
                this.onValueChange.emit(parameter);
            }
        );
        componentRef.changeDetectorRef.detectChanges();
        this._errorWrapperComponents.set(descriptor.ref.id, componentRef.instance);
        this._parameterComponents.set(descriptor.ref.id, componentRef.instance.parameterComponent);
    }

    public triggerInputChangeDetection() {
        let inputs = this.elem.nativeElement.querySelectorAll('input');
        inputs.forEach(input => {
            input.blur();

        })
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
