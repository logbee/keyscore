import {ComponentFactory, ComponentRef, Injectable, ViewContainerRef} from "@angular/core";
import {ParameterComponent} from "../parameters/ParameterComponent";
import {ParameterDescriptor, Parameter} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter.model";

@Injectable({
    providedIn: 'root'
})
export class ParameterComponentFactoryService {
    private componentFactories:
        Map<string, (containerRef: ViewContainerRef) =>
            ComponentRef<ParameterComponent<ParameterDescriptor, Parameter>>> = new Map();

    public register(jsonClass: string, f: (containerRef: ViewContainerRef) =>
        ComponentRef<ParameterComponent<ParameterDescriptor, Parameter>>) {
        console.log("Register Component Factory for: ", jsonClass);
        this.componentFactories.set(jsonClass, f);
    }

    public createParameterComponent(jsonClass: string, containerRef: ViewContainerRef)
        : ComponentRef<ParameterComponent<ParameterDescriptor, Parameter>> {

        const factory = this.componentFactories.get(jsonClass);
        if (factory) {
            return factory(containerRef);
        }
        throw Error(`No component factory found for: ${jsonClass}.
                              Maybe you forgot to register the component at the ParameterComponentFactoryService?`);
    }
}