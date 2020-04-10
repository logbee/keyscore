import {ChangeDetectorRef, Component, Input, ViewChild, ViewContainerRef} from "@angular/core";
import {ParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/ParameterComponent";
import {
    BooleanParameterCondition,
    ParameterGroup,
    ParameterGroupConditionJsonClass,
    ParameterGroupDescriptor
} from "@keyscore-manager-models/src/main/parameters/group-parameter.model";
import {BooleanParameter} from '@keyscore-manager-models/src/main/parameters/boolean-parameter.model'
import {ParameterComponentFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-component-factory.service";
import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {BehaviorSubject, Subject} from "rxjs";
import {filter, takeUntil} from "rxjs/operators";
import {cloneDeep} from 'lodash-es';
import {ParameterSet} from "@keyscore-manager-models/src/main/common/Configuration";

@Component({
    selector: 'parameter-group',
    template: `

        <div class="group-wrapper" *ngIf="(isVisible$|async) as visible" [class.without-name]="!descriptor.displayName">
            <span *ngIf="descriptor.displayName" class="group-label">{{descriptor.displayName}}</span>
            <ng-template #groupContainer></ng-template>
        </div>
    `,
    styleUrls: ['../../style/parameter-module-style.scss', './parameter-group.component.scss']
})
export class ParameterGroupComponent extends ParameterComponent<ParameterGroupDescriptor, ParameterGroup> {

    @Input() set conditionInput(val: Parameter) {
        this.isVisible$.next(this.checkCondition(val));
    };

    @ViewChild('groupContainer', { read: ViewContainerRef }) groupContainer: ViewContainerRef;

    isVisible$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    private _unsubscribe$: Subject<void> = new Subject();

    constructor(private parameterComponentFactory: ParameterComponentFactoryService, private cd: ChangeDetectorRef) {
        super();
    }

    protected onInit(): void {
        this.isVisible$.pipe(filter<boolean>(val => val), takeUntil(this._unsubscribe$)).subscribe(_ => {
            setTimeout(() => {
                this.createParameterComponents(this.parameter.value.parameters)
            }, 0)
        });
        if (!this.descriptor.condition) {
            this.isVisible$.next(true);
        }

    }

    createParameterComponents(parameters: Parameter[]) {
        this.groupContainer.clear();
        parameters.forEach(param => {
            const descriptor = this.descriptor.parameters.find(descr => descr.ref.id === param.ref.id);
            this.createParameterComponent(param, descriptor);
        });
        this.cd.detectChanges();
    }

    createParameterComponent(parameter: Parameter, descriptor: ParameterDescriptor) {
        const componentRef =
            this.parameterComponentFactory.createParameterComponent(descriptor.jsonClass, this.groupContainer);
        componentRef.instance.parameter = parameter;
        componentRef.instance.descriptor = descriptor;
        componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;

        componentRef.instance.emitter.pipe(takeUntil(this._unsubscribe$)).subscribe((componentParameter: Parameter) => {
            const groupParams: ParameterSet = cloneDeep(this.parameter.value);
            const parameterIndex = groupParams.parameters.findIndex(param => param.ref.id === componentParameter.ref.id);
            if (parameterIndex > -1) {
                groupParams.parameters.splice(parameterIndex, 1, componentParameter);
                this.onChange(new ParameterGroup(this.parameter.ref, groupParams));
            }
        })
    }

    onChange(parameter: ParameterGroup) {
        this.parameter = parameter;
        this.emitter.emit(parameter);
    }

    isVisible(): boolean {
        return this.isVisible$.getValue();
    }

    private checkCondition(conditionValue: Parameter): boolean {
        if (!this.descriptor.condition) return true;
        switch (this.descriptor.condition.jsonClass) {
            case ParameterGroupConditionJsonClass.BooleanCondition : {
                const param = (conditionValue as BooleanParameter);
                return (this.descriptor.condition as BooleanParameterCondition).negate != param.value;
            }
            default:
                return;
        }
    }

    protected onDestroy(): void {
        super.onDestroy();
        this._unsubscribe$.next();
        this._unsubscribe$.complete();
    }
}
