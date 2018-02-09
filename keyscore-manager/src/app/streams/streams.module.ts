import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {StreamsComponent} from "./streams.component";
import {CommonModule} from "@angular/common";
import {StreamEditorComponent} from "./stream-editor/stream-editor.component";
import {FormsModule} from "@angular/forms";
import {StreamsReducer} from "./streams.reducer";
import {StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {StreamsEffects} from "./streams.effects";
import {StreamDetailsComponent} from "./stream-editor/stream-details.component";

export const routes: Routes = [
    {path: '', component: StreamsComponent},
    {path: ':id', component: StreamEditorComponent},
];

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature('streams', StreamsReducer),
        EffectsModule.forFeature([StreamsEffects])
    ],
    declarations: [
        StreamsComponent,
        StreamEditorComponent,
        StreamDetailsComponent
    ],
    providers: []
})
export class StreamsModule { }