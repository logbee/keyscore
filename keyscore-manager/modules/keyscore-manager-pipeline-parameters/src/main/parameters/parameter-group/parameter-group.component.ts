import {Component, Input, ViewChild, ViewContainerRef} from "@angular/core";
import {ParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/ParameterComponent";
import {
    ParameterGroup,
    ParameterGroupDescriptor,
    ParameterGroupConditionJsonClass,
    BooleanParameterCondition
} from "@keyscore-manager-models/src/main/parameters/group-parameter.model";
import {BooleanParameter} from '@keyscore-manager-models/src/main/parameters/boolean-parameter.model'
import {ParameterComponentFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-component-factory.service";
import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";
import * as _ from 'lodash';

@Component({
    selector: 'parameter-group',
    template: `
        <div class="group-wrapper" *ngIf="_isVisible">
            <span class="group-label">{{descriptor.displayName}}</span>
            <ng-template #groupContainer></ng-template>
        </div>

    `,
    styleUrls: ['../../style/parameter-module-style.scss', './parameter-group.component.scss']
})
export class ParameterGroupComponent extends ParameterComponent<ParameterGroupDescriptor, ParameterGroup> {

    @Input() set conditionInput(val: Parameter) {
        this._isVisible = this.checkCondition(val);
    };

    private _isVisible: boolean = false;


    @ViewChild('groupContainer', {read: ViewContainerRef}) groupContainer: ViewContainerRef;

    private unsubscribe$: Subject<void> = new Subject();

    constructor(private parameterComponentFactory: ParameterComponentFactoryService) {
        super();
    }

    protected onInit(): void {
        this.createParameterComponents(this.parameter.value);
    }

    createParameterComponents(parameters: Parameter[]) {
        parameters.forEach(param => {
            const descriptor = this.descriptor.parameters.find(descr => descr.ref.id === param.ref.id);
            this.createParameterComponent(param, descriptor);
        })
    }

    createParameterComponent(parameter: Parameter, descriptor: ParameterDescriptor) {
        const componentRef =
            this.parameterComponentFactory.createParameterComponent(descriptor.jsonClass, this.groupContainer);
        componentRef.instance.parameter = parameter;
        componentRef.instance.descriptor = descriptor;
        componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;

        componentRef.instance.emitter.pipe(takeUntil(this.unsubscribe$)).subscribe(compParameter => {
            const groupParams: Parameter[] = _.cloneDeep(this.parameter.value);
            const parameterIndex = groupParams.findIndex(param => param.ref.id === compParameter.ref.id);
            if (parameterIndex > -1) {
                groupParams.splice(parameterIndex, 1, compParameter);
                this.onChange(new ParameterGroup(this.parameter.ref, groupParams));
            }
        })
    }

    onChange(parameter: ParameterGroup) {
        this.emitter.emit(parameter);
    }

    private checkCondition(conditionValue: Parameter): boolean {
        if (!this.descriptor.condition) return true;
        switch (this.descriptor.condition.jsonClass) {
            case(ParameterGroupConditionJsonClass.BooleanCondition): {
                const param = (conditionValue as BooleanParameter);
                return (this.descriptor.condition as BooleanParameterCondition).negate ? !param.value : param.value;
            }
            default:
                return;
        }
    }

}
