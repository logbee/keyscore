import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MaterialModule} from "keyscore-manager-material";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ParameterFactoryService} from "../../service/parameter-factory.service";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {DecimalParameterComponent} from "./decimal-parameter.component";
import {DecimalParameter, DecimalParameterDescriptor, JSONCLASS_DECIMAL_DESCR} from "./decimal-parameter.model";
import {SharedControlsModule} from "../../shared-controls/shared-controls.module";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule,
        SharedControlsModule
    ],
    declarations: [
        DecimalParameterComponent
    ],
    entryComponents: [
        DecimalParameterComponent
    ],
    exports: [
        DecimalParameterComponent
    ],
    providers: []
})
export class DecimalParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_DECIMAL_DESCR, (descriptor: DecimalParameterDescriptor) => {
            return new DecimalParameter(descriptor.ref,descriptor.defaultValue || 0);
        });

        this.componentFactory.register(JSONCLASS_DECIMAL_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(DecimalParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }
}