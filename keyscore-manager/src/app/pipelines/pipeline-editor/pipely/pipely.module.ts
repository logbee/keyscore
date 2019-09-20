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
import {PuzzleBoxComponent} from "./puzzle-box/puzzle-box.component";
import {PuzzleCategoryComponent} from "./puzzle-box/puzzle-category.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {PipelineConfiguratorService} from "./services/pipeline-configurator.service";
import {ConfiguratorModule} from "./configurator/configurator.module";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";
import {ParameterModule} from "@/../modules/keyscore-manager-pipeline-parameters/src/main/parameter.module";
import {DataPreviewModule} from "@keyscore-manager-data-preview/src/main/data-preview.module";


@NgModule({
    imports: [
        CommonModule,
        TranslateModule,
        MaterialModule,
        ParameterModule,
        ReactiveFormsModule,
        ConfiguratorModule,
        FormsModule,
        DataPreviewModule
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
        DropzoneFactory,
        PipelineConfiguratorService

    ]

})

export class PipelyModule {

}