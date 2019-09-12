import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TextParameterModule} from "../text-parameter/text-parameter.module";
import {ListParameterComponent} from "./list-parameter.component";
import {FieldNameParameterModule} from "../field-name-parameter/field-name-parameter.module";

import {FieldParameterModule} from "../field-parameter/field-parameter.module";
import {
    JSONCLASS_TEXTLIST_DESCR,
    TextListParameter,
    TextListParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/text-list-parameter.model";
import {
    FieldNameListParameter,
    FieldNameListParameterDescriptor,
    JSONCLASS_FIELDNAMELIST_DESCR
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/field-name-list-parameter.model";
import {
    FieldListParameter,
    FieldListParameterDescriptor,
    JSONCLASS_FIELDLIST_DESCR
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/field-list-parameter.model";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";
import {Field} from "@/../modules/keyscore-manager-models/src/main/dataset/Field";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        DragDropModule,
        TextParameterModule,
        FieldNameParameterModule,
        FieldParameterModule
    ],
    declarations: [ListParameterComponent],
    exports: [ListParameterComponent],
    entryComponents: [ListParameterComponent]
})
export class ListParameterModule {

    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        //TextList
        this.factory.register(JSONCLASS_TEXTLIST_DESCR, (descriptor: TextListParameterDescriptor, value: string[] = []) => {
            return new TextListParameter(descriptor.ref, value);
        });

        this.componentFactory.register(JSONCLASS_TEXTLIST_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(ListParameterComponent);
            return containerRef.createComponent(compFactory);
        });
        //FieldNameList
        this.factory.register(JSONCLASS_FIELDNAMELIST_DESCR, (descriptor: FieldNameListParameterDescriptor, value: string[] = []) => {
            return new FieldNameListParameter(descriptor.ref, value);
        });

        this.componentFactory.register(JSONCLASS_FIELDNAMELIST_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(ListParameterComponent);
            return containerRef.createComponent(compFactory);
        });
        //FieldList
        this.factory.register(JSONCLASS_FIELDLIST_DESCR, (descriptor: FieldListParameterDescriptor, value: Field[] = []) => {
            return new FieldListParameter(descriptor.ref, value);
        });

        this.componentFactory.register(JSONCLASS_FIELDLIST_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(ListParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }

}