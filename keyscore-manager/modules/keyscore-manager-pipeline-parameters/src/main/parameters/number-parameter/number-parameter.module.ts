import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {NumberParameterComponent} from "./number-parameter.component";
import {
    JSONCLASS_NUMBER_DESCR,
    NumberParameter,
    NumberParameterDescriptor
} from "@keyscore-manager-models/src/main/parameters/number-parameter.model";
import {SharedControlsModule} from "../../shared-controls/shared-controls.module";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        SharedControlsModule
    ],
    declarations: [
        NumberParameterComponent
    ],
    entryComponents: [
        NumberParameterComponent
    ],
    exports: [
        NumberParameterComponent
    ],
    providers: []
})
export class NumberParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_NUMBER_DESCR, (descriptor: NumberParameterDescriptor) => {
            return new NumberParameter(descriptor.ref, descriptor.defaultValue || 0);
        });

        this.componentFactory.register(JSONCLASS_NUMBER_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(NumberParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }
}