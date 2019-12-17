import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {DirectiveComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/directive/directive.component";
import {CommonModule} from "@angular/common";
import {MaterialModule} from '@keyscore-manager-material/src/main/material.module';
import {DirectiveSequenceParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/directive-sequence-parameter.component";
import {DirectiveSequenceComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/directive-sequence/directive-sequence.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {AddDirectiveComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/add-directive/add-directive.component";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {ParameterComponentFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-component-factory.service";
import {DecimalParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/decimal-parameter/decimal-parameter.component";
import {
    JSONCLASS_DIRECTIVE_SEQ_DESCR,
    FieldDirectiveSequenceParameterDescriptor,
    FieldDirectiveSequenceParameter,
    FieldDirectiveDescriptor,
    FieldDirectiveSequenceConfiguration
} from "@keyscore-manager-models/src/main/parameters/directive.model"

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        DragDropModule
    ],
    declarations: [
        DirectiveComponent,
        DirectiveSequenceComponent,
        DirectiveSequenceParameterComponent,
        AddDirectiveComponent
    ],
    entryComponents: [
        DirectiveComponent,
        DirectiveSequenceComponent,
        DirectiveSequenceParameterComponent
    ],
    exports: [
        DirectiveSequenceParameterComponent
    ]
})
export class DirectiveSequenceParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_DIRECTIVE_SEQ_DESCR, (descriptor: FieldDirectiveSequenceParameterDescriptor, value: FieldDirectiveSequenceConfiguration[] = []) => {
            return new FieldDirectiveSequenceParameter(descriptor.ref, value);
        });

        this.componentFactory.register(JSONCLASS_DIRECTIVE_SEQ_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(DirectiveSequenceParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }
}
