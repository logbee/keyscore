import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {StreamsComponent} from "./streams.component";
import {CommonModule} from "@angular/common";
import {StreamEditorComponent} from "./stream-editor/stream-editor.component";
import {FormsModule} from "@angular/forms";

export const routes: Routes = [
    {path: '', component: StreamsComponent},
    {path: ':id', component: StreamEditorComponent},
];

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild(routes)
    ],
    declarations: [
        StreamsComponent,
        StreamEditorComponent
    ],
    providers: []
})
export class StreamsModule { }