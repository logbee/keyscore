import {NgModule} from "@angular/core";
import {DirectiveComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/directive/directive.component";
import {CommonModule} from "@angular/common";
import {MaterialModule} from '@keyscore-manager-material/src/main/material.module';
import {DirectiveSequenceParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/directive-sequence-parameter.component";
import {DirectiveSequenceComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/directive-sequence/directive-sequence.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {AddDirectiveComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/add-directive/add-directive.component";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        DragDropModule,
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
        DirectiveSequenceParameterComponent,
        AddDirectiveComponent
    ]
})
export class DirectiveSequenceParameterModule{

}
