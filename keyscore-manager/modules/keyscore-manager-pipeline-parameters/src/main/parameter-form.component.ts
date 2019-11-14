import {
    ChangeDetectionStrategy, ChangeDetectorRef,
    Component, ElementRef,
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

    @Output() onValueChange: EventEmitter<Parameter> = new EventEmitter();

    @ViewChild("formContainer", {read: ViewContainerRef}) formContainer: ViewContainerRef;

    private unsubscribe$: Subject<void> = new Subject<void>();

    private parameterComponents: Map<string, ParameterComponent<ParameterDescriptor, Parameter>> = new Map();

    constructor(private parameterComponentFactory: ParameterComponentFactoryService, private cd: ChangeDetectorRef, private renderer: Renderer2, private elem: ElementRef) {
    }

    ngOnInit() {

    }

    createParameterComponents(parameters: ParameterMap, clearContainer: boolean = true) {

        if (clearContainer) {
            this.unsubscribe$.next();
            this.formContainer.clear();
        }
        if (parameters && parameters.parameters) {
            Object.entries(parameters.parameters).forEach(([ref, [parameter, descriptor]]) => {
                this.createParameterComponent(parameter, descriptor);
            })
        }
    }

    createParameterComponent(parameter: Parameter, descriptor: ParameterDescriptor) {
        if (descriptor.jsonClass === ParameterDescriptorJsonClass.ParameterGroupDescriptor) {
            const groupDescriptor: ParameterGroupDescriptor = descriptor as ParameterGroupDescriptor;
            if (groupDescriptor.condition) {
                setTimeout(() => {
                    const groupComponent = this.parameterComponents.get(groupDescriptor.ref.id);
                    const conditionComponent = this.parameterComponents.get(groupDescriptor.condition.parameter.id);
                    if (!groupComponent || !conditionComponent) return;
                    conditionComponent.emitter.pipe(takeUntil(this.unsubscribe$)).subscribe((parameter: Parameter) => {
                        (groupComponent as ParameterGroupComponent).conditionInput = parameter;
                    });
                }, 0)
            }
        }

        const componentRef = this.parameterComponentFactory.createParameterComponent(descriptor.jsonClass, this.formContainer);
        componentRef.instance.parameter = parameter;
        componentRef.instance.descriptor = descriptor;
        componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;

        componentRef.instance.emitter.pipe(takeUntil(this.unsubscribe$)).subscribe((parameter: Parameter) => {
                this.onValueChange.emit(parameter);
            }
        );

        this.parameterComponents.set(descriptor.ref.id, componentRef.instance);
    }

    ngOnDestroy() {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public triggerInputChangeDetection() {
        let inputs = this.elem.nativeElement.querySelectorAll('input');
        inputs.forEach(input => {
            input.blur();

        })
    }
}
