import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {TranslateModule} from "@ngx-translate/core";
import {WorkspaceComponent} from "./workspace.component";
import {DraggableComponent} from "./draggable.component";
import {DropzoneComponent} from "./dropzone.component";
import {DropzoneFactory} from "./dropzone/dropzone-factory";
import {ToolbarDropzoneSubcomponent} from "./dropzone/toolbar-dropzone-subcomponent";
import {WorkspaceDropzoneSubcomponent} from "./dropzone/workspace-dropzone-subcomponent";
import {ConnectorDropzoneSubcomponent} from "./dropzone/connector-dropzone-subcomponent";
import {DraggableFactory} from "./draggable/draggable-factory";
import {TrashDropzoneSubcomponent} from "./dropzone/trash-dropzone-subcomponent";
import {HeaderBarModule} from "../../../common/headerbar.module";

@NgModule({
    imports: [
        CommonModule,
        TranslateModule
    ],
    declarations: [
        WorkspaceComponent,
        DraggableComponent,
        DropzoneComponent,
        ToolbarDropzoneSubcomponent,
        WorkspaceDropzoneSubcomponent,
        ConnectorDropzoneSubcomponent,
        TrashDropzoneSubcomponent
    ],
    entryComponents: [
        DraggableComponent,
        DropzoneComponent,
        ToolbarDropzoneSubcomponent,
        WorkspaceDropzoneSubcomponent,
        ConnectorDropzoneSubcomponent,
        TrashDropzoneSubcomponent

    ],
    exports: [WorkspaceComponent],
    providers: [
        DraggableFactory,
        DropzoneFactory

    ]

})

export class PipelyModule {

}