import {Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild, ViewContainerRef} from "@angular/core";
import {ParameterComponentFactoryService} from "./service/parameter-component-factory.service";
import {Parameter, ParameterMap} from "./parameters/parameter.model";
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
        this.parameters.refs.forEach(ref => {
            const parameterDescriptorTuple = this.parameters.parameters[ref];

            const componentRef = this.parameterComponentFactory.createParameterComponent(parameterDescriptorTuple[1].jsonClass, this.formContainer);
            componentRef.instance.parameter = parameterDescriptorTuple[0];
            componentRef.instance.descriptor = parameterDescriptorTuple[1];
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