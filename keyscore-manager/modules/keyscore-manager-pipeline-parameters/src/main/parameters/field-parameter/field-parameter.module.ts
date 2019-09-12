import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {SharedControlsModule} from "../../shared-controls/shared-controls.module";
import {FieldParameterComponent} from "./field-parameter.component";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {ValueControlsModule} from "../../value-controls/value-controls.module";
import {
    FieldParameter,
    FieldParameterDescriptor,
    JSONCLASS_FIELD_DESCR
} from "@/../modules/keyscore-manager-models/src/main/parameters/field-parameter.model";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";
import {Field} from "@/../modules/keyscore-manager-models/src/main/dataset/Field";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        SharedControlsModule,
        ValueControlsModule
    ],
    declarations: [FieldParameterComponent],
    exports: [FieldParameterComponent],
    entryComponents: [FieldParameterComponent]
})
export class FieldParameterModule {

    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_FIELD_DESCR, (descriptor: FieldParameterDescriptor,value:Field=new Field(descriptor.defaultName,null)) => {
            return new FieldParameter(descriptor.ref, value);
        });

        this.componentFactory.register(JSONCLASS_FIELD_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(FieldParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }

}