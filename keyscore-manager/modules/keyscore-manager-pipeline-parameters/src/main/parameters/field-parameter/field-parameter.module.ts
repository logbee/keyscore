import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MaterialModule} from "keyscore-manager-material";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {SharedControlsModule} from "../../shared-controls/shared-controls.module";
import {FieldParameterComponent} from "./field-parameter.component";
import {ParameterFactoryService} from "../../service/parameter-factory.service";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {FieldParameter, FieldParameterDescriptor, JSONCLASS_FIELD_DESCR} from "./field-parameter.model";
import {ValueControlsModule} from "../../value-controls/value-controls.module";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule,
        SharedControlsModule,
        ValueControlsModule
    ],
    declarations: [FieldParameterComponent],
    exports: [FieldParameterComponent],
    entryComponents: [FieldParameterComponent]
})
export class FieldParameterModule {

    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_FIELD_DESCR, (descriptor: FieldParameterDescriptor) => {
            return new FieldParameter(descriptor.ref, {name: descriptor.defaultName || "", value: null});
        });

        this.componentFactory.register(JSONCLASS_FIELD_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(FieldParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }

}