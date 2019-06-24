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
    exports: [
        ExpressionParameterComponent
    ],
    providers: []
})
export class ExpressionParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(ExpressionParameterDescriptor.jsonClass, (descriptor: ExpressionParameterDescriptor) => {
            return new ExpressionParameter(descriptor.ref, "", null);
        });

        this.componentFactory.register(ExpressionParameterDescriptor.jsonClass, (containerRef:ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(ExpressionParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }
}