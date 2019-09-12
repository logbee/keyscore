import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {ExpressionParameterComponent} from "./expression-parameter.component";
import {
    ExpressionParameter,
    ExpressionParameterDescriptor,
    JSONCLASS_EXPRESSION_DESCR
} from "@keyscore-manager-models/src/main/parameters/expression-parameter.model";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule
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
        this.factory.register(JSONCLASS_EXPRESSION_DESCR, (descriptor: ExpressionParameterDescriptor) => {
            return new ExpressionParameter(descriptor.ref, descriptor.defaultValue || "", null);
        });

        this.componentFactory.register(JSONCLASS_EXPRESSION_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(ExpressionParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }
}