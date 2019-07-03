import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MaterialModule} from "keyscore-manager-material";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ParameterFactoryService} from "../../service/parameter-factory.service";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {NumberParameterComponent} from "./number-parameter.component";
import {JSONCLASS_NUMBER_DESCR, NumberParameter, NumberParameterDescriptor} from "./number-parameter.model";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule
    ],
    declarations: [
        NumberParameterComponent
    ],
    entryComponents: [
        NumberParameterComponent
    ],
    exports: [
        NumberParameterComponent
    ],
    providers: []
})
export class NumberParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_NUMBER_DESCR, (descriptor: NumberParameterDescriptor) => {
            return new NumberParameter(descriptor.ref, descriptor.defaultValue || 0);
        });

        this.componentFactory.register(JSONCLASS_NUMBER_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(NumberParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }
}