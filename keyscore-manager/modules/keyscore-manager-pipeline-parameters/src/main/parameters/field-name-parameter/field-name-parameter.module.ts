import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {FieldNameParameterComponent} from "./field-name-parameter.component";
import {
    FieldNameParameter,
    FieldNameParameterDescriptor,
    JSONCLASS_FIELDNAME_DESCR
} from "@keyscore-manager-models/src/main/parameters/field-name-parameter.model";
import {SharedControlsModule} from "../../shared-controls/shared-controls.module";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        SharedControlsModule
    ],
    declarations: [FieldNameParameterComponent],
    entryComponents: [FieldNameParameterComponent],
    exports: [FieldNameParameterComponent]

})
export class FieldNameParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_FIELDNAME_DESCR, (descriptor: FieldNameParameterDescriptor, value?:string) => {
            return new FieldNameParameter(descriptor.ref, value === null ? descriptor.defaultValue : (value === undefined ? '' : value));
        });
        this.componentFactory.register(JSONCLASS_FIELDNAME_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(FieldNameParameterComponent);
            return containerRef.createComponent(compFactory);
        });

    }
}