import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {ParameterFactoryService} from "../../service/parameter-factory.service";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {TextParameterComponent} from "./text-parameter.component";
import {MaterialModule} from "keyscore-manager-material";
import {
    JSONCLASS_TEXT_DESCR,
    TextParameter,
    TextParameterDescriptor
} from "./text-parameter.model";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {StringValidatorService} from "../../service/string-validator.service";

@NgModule({
    imports: [
        CommonModule,
        BrowserAnimationsModule,
        MaterialModule
    ],
    declarations: [TextParameterComponent],
    entryComponents: [TextParameterComponent],
    exports: [TextParameterComponent],
    providers: [StringValidatorService]

})
export class TextParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_TEXT_DESCR, (descriptor: TextParameterDescriptor, value?: string) => {
            return new TextParameter(descriptor.ref, value === null ? descriptor.defaultValue : (value === undefined ? '' : value));
        });
        this.componentFactory.register(JSONCLASS_TEXT_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(TextParameterComponent);
            return containerRef.createComponent(compFactory);
        });

    }
}