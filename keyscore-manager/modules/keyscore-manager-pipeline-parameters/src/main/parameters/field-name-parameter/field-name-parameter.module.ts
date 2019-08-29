import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {ParameterFactoryService} from "../../service/parameter-factory.service";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MaterialModule} from "@keyscore-manager-material";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {FieldNameParameterComponent} from "./field-name-parameter.component";
import {
    FieldNameParameter,
    FieldNameParameterDescriptor,
    JSONCLASS_FIELDNAME_DESCR
} from "./field-name-parameter.model";
import {SharedControlsModule} from "../../shared-controls/shared-controls.module";

@NgModule({
    imports: [
        CommonModule,
        BrowserAnimationsModule,
        MaterialModule,
        SharedControlsModule
    ],
    declarations: [FieldNameParameterComponent],
    entryComponents: [FieldNameParameterComponent],
    exports: [FieldNameParameterComponent]

})
export class FieldNameParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_FIELDNAME_DESCR, (descriptor: FieldNameParameterDescriptor, value?:string) => {
            return new FieldNameParameter(descriptor.ref, value === null ? descriptor.defaultValue : (value === undefined ? '' : value));
        });
        this.componentFactory.register(JSONCLASS_FIELDNAME_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(FieldNameParameterComponent);
            return containerRef.createComponent(compFactory);
        });

    }
}