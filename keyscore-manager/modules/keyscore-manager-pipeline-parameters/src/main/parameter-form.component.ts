import {Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild, ViewContainerRef} from "@angular/core";
import {ParameterComponentFactoryService} from "./service/parameter-component-factory.service";
import {Parameter, ParameterMap} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {of, Subscription} from "rxjs";

@Component({
    selector: 'parameter-form',
    template: `
        <ng-template #formContainer></ng-template>
    `
})
export class ParameterFormComponent implements OnInit, OnDestroy {
    @Input() parameters: ParameterMap;
    @Input() autoCompleteDataList:string[] = [];

    @Output() onValueChange: EventEmitter<Parameter> = new EventEmitter();

    @ViewChild("formContainer", {read: ViewContainerRef}) formContainer: ViewContainerRef;

    private subs: Subscription[] = [];

    constructor(private parameterComponentFactory: ParameterComponentFactoryService) {
    }

    ngOnInit() {
        Object.entries(this.parameters.parameters).forEach(([ref,[parameter,descriptor]]) => {
            const componentRef = this.parameterComponentFactory.createParameterComponent(descriptor.jsonClass, this.formContainer);
            componentRef.instance.parameter = parameter;
            componentRef.instance.descriptor = descriptor;
            componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;

            const sub = componentRef.instance.emitter.subscribe(parameter =>
                this.onValueChange.emit(parameter));

            this.subs.push(sub);
        })
    }

    ngOnDestroy() {
        this.subs.forEach(sub => sub.unsubscribe());
    }


}