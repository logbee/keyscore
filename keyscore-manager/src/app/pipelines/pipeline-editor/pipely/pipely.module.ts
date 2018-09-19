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
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {ConnectorComponent} from "./connectors/connector.component";
import {MaterialModule} from "../../../material.module";
import {ConfigurationComponent} from "./configuration.component";
import {ParameterModule} from "../../../common/configuration/parameter/parameter.module";
import {PuzzleBoxComponent} from "./puzzle-box/puzzle-box.component";
import {PuzzleCategoryComponent} from "./puzzle-box/puzzle-category.component";


@NgModule({
    imports: [
        CommonModule,
        TranslateModule,
        MaterialModule,
        ParameterModule
    ],
    declarations: [
        WorkspaceComponent,
        DraggableComponent,
        DropzoneComponent,
        ToolbarDropzoneSubcomponent,
        WorkspaceDropzoneSubcomponent,
        ConnectorDropzoneSubcomponent,
        TrashDropzoneSubcomponent,
        ConnectorComponent,
        ConfigurationComponent,
        PuzzleBoxComponent,
        PuzzleCategoryComponent
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