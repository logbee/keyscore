import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {ParameterFactoryService} from "../../service/parameter-factory.service";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MaterialModule} from "keyscore-manager-material";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {FieldNameParameterComponent} from "./field-name-parameter.component";
import {FieldNameParameter, FieldNameParameterDescriptor, JSONCLASS_FIELDNAME_DESCR} from "./fieldname-parameter.model";

@NgModule({
    imports: [
        CommonModule,
        BrowserAnimationsModule,
        MaterialModule
    ],
    declarations: [FieldNameParameterComponent],
    entryComponents: [FieldNameParameterComponent],
    exports: [FieldNameParameterComponent]

})
export class TextParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_FIELDNAME_DESCR, (descriptor: FieldNameParameterDescriptor) => {
            return new FieldNameParameter(descriptor.ref, "");
        });
        this.componentFactory.register(JSONCLASS_FIELDNAME_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(FieldNameParameterComponent);
            return containerRef.createComponent(compFactory);
        });

    }
}