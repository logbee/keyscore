import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {FieldNamePatternParameterComponent} from "./field-name-pattern-parameter.component";
import {
    FieldNamePatternParameter,
    FieldNamePatternParameterDescriptor,
    JSONCLASS_FIELDNAMEPATTERN_DESCR
} from "@keyscore-manager-models/src/main/parameters/field-name-pattern-parameter.model";
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
        FieldNamePatternParameterComponent
    ],
    entryComponents: [
        FieldNamePatternParameterComponent
    ],
    exports: [
        FieldNamePatternParameterComponent
    ],
    providers: []
})
export class FieldNamePatternParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_FIELDNAMEPATTERN_DESCR, (descriptor: FieldNamePatternParameterDescriptor) => {
            return new FieldNamePatternParameter(descriptor.ref, descriptor.defaultValue || "", null);
        });

        this.componentFactory.register(JSONCLASS_FIELDNAMEPATTERN_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(FieldNamePatternParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }
}