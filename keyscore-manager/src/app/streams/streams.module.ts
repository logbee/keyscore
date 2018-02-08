import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {StreamsComponent} from "./streams.component";
import {CommonModule} from "@angular/common";
import {StreamEditorComponent} from "./stream-editor/stream-editor.component";
import {FormsModule} from "@angular/forms";
import {streamsReducers} from "./streams.reducer";
import {StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {StreamsEffects} from "./streams.effects";

export const routes: Routes = [
    {path: '', component: StreamsComponent},
    {path: ':id', component: StreamEditorComponent},
];

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature('streams', streamsReducers),
        EffectsModule.forFeature([StreamsEffects])
    ],
    declarations: [
        StreamsComponent,
        StreamEditorComponent
    ],
    providers: []
})
export class StreamsModule { }