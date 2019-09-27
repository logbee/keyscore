import {Component, ViewChild, ViewContainerRef} from "@angular/core";
import {ParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/ParameterComponent";
import {
    ParameterGroup,
    ParameterGroupDescriptor
} from "@keyscore-manager-models/src/main/parameters/group-parameter.model";
import {ParameterComponentFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-component-factory.service";
import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";
import * as _ from 'lodash';

@Component({
    selector: 'parameter-group',
    template: `
        <div class="group-wrapper">
            <span class="group-label">{{descriptor.displayName}}</span>
            <ng-template #groupContainer></ng-template>
        </div>

    `,
    styleUrls: ['../../style/parameter-module-style.scss', './parameter-group.component.scss']
})
export class ParameterGroupComponent extends ParameterComponent<ParameterGroupDescriptor, ParameterGroup> {

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

}