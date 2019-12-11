import {AfterViewInit, ChangeDetectorRef, Component, Input, ViewChild, ViewContainerRef} from "@angular/core";
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
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {filter, share, take, takeUntil} from "rxjs/operators";
import * as _ from 'lodash';
import {ParameterSet} from "@keyscore-manager-models/src/main/common/Configuration";

@Component({
    selector: 'parameter-group',
    template: `

        <div class="group-wrapper" *ngIf="(visible$|async) as visible" [class.without-name]="!descriptor.displayName">
            <span *ngIf="descriptor.displayName" class="group-label">{{descriptor.displayName}}</span>
            <ng-template #groupContainer></ng-template>
        </div>
    `,
    styleUrls: ['../../style/parameter-module-style.scss', './parameter-group.component.scss']
})
export class ParameterGroupComponent extends ParameterComponent<ParameterGroupDescriptor, ParameterGroup> {

    @Input() set conditionInput(val: Parameter) {
        this._isVisible.next(this.checkCondition(val));
    };

    private _isVisible: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    private visible$: Observable<boolean> = this._isVisible.asObservable().pipe(share<boolean>());

    @ViewChild('groupContainer', {read: ViewContainerRef}) groupContainer: ViewContainerRef;

    private unsubscribe$: Subject<void> = new Subject();

    constructor(private parameterComponentFactory: ParameterComponentFactoryService, private cd: ChangeDetectorRef) {
        super();
    }

    protected onInit(): void {
        this.visible$.pipe(filter<boolean>(val => val), takeUntil(this.unsubscribe$)).subscribe(_ => {
            setTimeout(() => {
                this.createParameterComponents(this.parameter.value.parameters)
            }, 0)
        });
        if (!this.descriptor.condition) {
            this._isVisible.next(true);
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

        componentRef.instance.emitter.pipe(takeUntil(this.unsubscribe$)).subscribe((componentParameter: Parameter) => {
            const groupParams: ParameterSet = _.cloneDeep(this.parameter.value);
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
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }
}
