import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {TextParameterComponent} from "./text-parameter.component";
import {
    JSONCLASS_TEXT_DESCR,
    TextParameter,
    TextParameterDescriptor
} from "@keyscore-manager-models/src/main/parameters/text-parameter.model";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {StringValidatorService} from "../../service/string-validator.service";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        TranslateModule
    ],
    declarations: [TextParameterComponent],
    entryComponents: [TextParameterComponent],
    exports: [TextParameterComponent],
    providers: [StringValidatorService]
})
export class TextParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_TEXT_DESCR, (descriptor: TextParameterDescriptor, value?: string) => {
            return new TextParameter(descriptor.ref, value === undefined ? descriptor.defaultValue : value);
        });
        this.componentFactory.register(JSONCLASS_TEXT_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(TextParameterComponent);
            return containerRef.createComponent(compFactory);
        });

    }
}
