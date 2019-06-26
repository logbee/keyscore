import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MaterialModule} from "keyscore-manager-material";
import {ExpressionParameterComponent} from "./expression-parameter.component";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ParameterFactoryService} from "../../service/parameter-factory.service";
import {ExpressionParameter, ExpressionParameterDescriptor} from "./expression-parameter.model";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule
    ],
    declarations: [
        ExpressionParameterComponent
    ],
    entryComponents: [
        ExpressionParameterComponent
    ],
    exports: [
        ExpressionParameterComponent
    ],
    providers: []
})
export class ExpressionParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register("io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor", (descriptor: ExpressionParameterDescriptor) => {
            return new ExpressionParameter(descriptor.ref, "", null);
        });

        this.componentFactory.register("io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor", (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(ExpressionParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }
}