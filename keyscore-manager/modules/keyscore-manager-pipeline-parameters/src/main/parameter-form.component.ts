import {Component, ComponentRef, Input, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {ParameterComponentFactoryService} from "./service/parameter-component-factory.service";
import {Parameter, ParameterDescriptor, ParameterMap} from "./parameters/parameter.model";
import {ParameterComponent} from "./parameters/ParameterComponent";

@Component({
    selector: 'parameter-form',
    template: `
        <h1>Parameter Form</h1>
        <ng-template #formContainer></ng-template>
    `
})
export class ParameterFormComponent implements OnInit {
    @Input() parameters: ParameterMap;

    @ViewChild("formContainer", {read: ViewContainerRef}) formContainer: ViewContainerRef;

    private components: ComponentRef<ParameterComponent<ParameterDescriptor, Parameter>>[];

    constructor(private parameterComponentFactory: ParameterComponentFactoryService) {
    }

    ngOnInit() {
        this.parameters.refs.forEach(ref => {
            let parameterDescriptorTuple = this.parameters.parameters[ref];
            console.log("Component creation for ", parameterDescriptorTuple[1].jsonClass);
            this.components.push(this.parameterComponentFactory.createParameterComponent(parameterDescriptorTuple[1].jsonClass, this.formContainer));
        })
    }


}