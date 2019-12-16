import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {BooleanParameterComponent} from "./boolean-parameter.component";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {
    BooleanParameter,
    BooleanParameterDescriptor,
    JSONCLASS_BOOLEAN_DESCR
} from "@/../modules/keyscore-manager-models/src/main/parameters/boolean-parameter.model";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
    ],
    declarations: [BooleanParameterComponent],
    entryComponents:[BooleanParameterComponent],
    exports: [BooleanParameterComponent]
})
export class BooleanParameterModule{
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_BOOLEAN_DESCR, (descriptor: BooleanParameterDescriptor) => {
            return new BooleanParameter(descriptor.ref, descriptor.defaultValue || false);
        });
        this.componentFactory.register(JSONCLASS_BOOLEAN_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(BooleanParameterComponent);
            return containerRef.createComponent(compFactory);
        });

    }
}
