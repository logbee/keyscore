import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {TranslateModule} from "@ngx-translate/core";
import {WorkspaceComponent} from "./workspace.component";
import {DraggableComponent} from "./draggable.component";
import {DropzoneComponent} from "./dropzone.component";

@NgModule({
    imports: [
        CommonModule,
        TranslateModule
    ],
    declarations: [
        WorkspaceComponent,
        DraggableComponent,
        DropzoneComponent
    ],
    entryComponents: [
        DraggableComponent,
        DropzoneComponent
    ],
    exports: [WorkspaceComponent]

})

export class PipelyModule {

}