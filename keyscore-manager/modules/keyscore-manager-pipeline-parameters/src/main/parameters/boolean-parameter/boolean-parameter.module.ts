import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MaterialModule} from "@keyscore-manager-material";
import {BooleanParameterComponent} from "./boolean-parameter.component";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {JSONCLASS_BOOLEAN_DESCR, BooleanParameterDescriptor, BooleanParameter} from "@/../modules/keyscore-manager-models/src/main/parameters/boolean-parameter.model";

@NgModule({
    imports: [
        CommonModule,
        BrowserAnimationsModule,
        MaterialModule
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