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
import {ConnectorComponent} from "./connectors/connector.component";
import {MaterialModule} from "../../../material.module";
import {ConfiguratorComponent} from "./configurator.component";
import {PuzzleBoxComponent} from "./puzzle-box/puzzle-box.component";
import {PuzzleCategoryComponent} from "./puzzle-box/puzzle-category.component";
import {ConfigurationModule} from "../../../common/configuration/configuration.module";


@NgModule({
    imports: [
        CommonModule,
        TranslateModule,
        MaterialModule,
        ConfigurationModule
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
        ConfiguratorComponent,
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