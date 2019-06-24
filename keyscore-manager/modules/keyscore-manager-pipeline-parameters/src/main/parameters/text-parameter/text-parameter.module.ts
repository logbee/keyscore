import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {ParameterFactoryService} from "../../service/parameter-factory.service";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {TextParameterComponent} from "./text-parameter.component";
import {MaterialModule} from "keyscore-manager-material";
import {TextParameter, TextParameterDescriptor} from "./text-parameter.model";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";

@NgModule({
    imports: [
        CommonModule,
        BrowserAnimationsModule,
        MaterialModule
    ],
    declarations: [TextParameterComponent],
    exports: [TextParameterComponent]

})
export class TextParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register("io.logbee.keyscore.model.descriptor.TextParameterDescriptor", (descriptor: TextParameterDescriptor) => {
            console.log("Created TextParameter from TextParameterDescriptor");
            return new TextParameter(descriptor.ref, "");
        });
        this.componentFactory.register(TextParameterDescriptor.jsonClass, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(TextParameterComponent);
            return containerRef.createComponent(compFactory);
        });

    }
}