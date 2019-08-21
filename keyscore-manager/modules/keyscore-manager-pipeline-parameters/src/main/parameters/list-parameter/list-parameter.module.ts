import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MaterialModule} from "keyscore-manager-material";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ParameterFactoryService} from "../../service/parameter-factory.service";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {JSONCLASS_TEXTLIST_DESCR, TextListParameter, TextListParameterDescriptor} from "./models/text-list-parameter.model";
import {TextParameterModule} from "../text-parameter/text-parameter.module";
import {ListParameterComponent} from "./list-parameter.component";
import {
    FieldNameListParameter,
    FieldNameListParameterDescriptor,
    JSONCLASS_FIELDNAMELIST_DESCR
} from "./models/field-name-list-parameter.model";
import {FieldNameParameterModule} from "../field-name-parameter/field-name-parameter.module";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule,
        DragDropModule,
        TextParameterModule,
        FieldNameParameterModule
    ],
    declarations: [ListParameterComponent],
    exports: [ListParameterComponent],
    entryComponents: [ListParameterComponent]
})
export class ListParameterModule {

    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        //TextList
        this.factory.register(JSONCLASS_TEXTLIST_DESCR, (descriptor: TextListParameterDescriptor,value:string[]=[]) => {
            return new TextListParameter(descriptor.ref, value);
        });

        this.componentFactory.register(JSONCLASS_TEXTLIST_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(ListParameterComponent);
            return containerRef.createComponent(compFactory);
        });
        //FieldNameList
        this.factory.register(JSONCLASS_FIELDNAMELIST_DESCR, (descriptor: FieldNameListParameterDescriptor,value:string[]=[]) => {
            return new FieldNameListParameter(descriptor.ref, value);
        });

        this.componentFactory.register(JSONCLASS_FIELDNAMELIST_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(ListParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }

}