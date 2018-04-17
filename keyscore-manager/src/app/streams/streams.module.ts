import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {StreamsComponent} from "./streams.component";
import {CommonModule} from "@angular/common";
import {StreamEditorComponent} from "./stream-editor/stream-editor.component";
import {FormsModule,ReactiveFormsModule} from "@angular/forms";
import {StreamsReducer} from "./streams.reducer";
import {StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {StreamsEffects} from "./streams.effects";
import {StreamDetailsComponent} from "./stream-editor/stream-details.component";
import {StreamFilterComponent} from "./stream-editor/stream-filter.component";
import {ParameterList} from "./stream-editor/filter-editor/parameter-list/parameter-list.component";
import {ParameterComponent} from "./stream-editor/filter-editor/parameter.component";
import {ParameterMap} from "./stream-editor/filter-editor/parameter-map.component";
import {BlocklyComponent} from "./stream-editor/blockly/blockly.component";

export const routes: Routes = [
    {path: '', component: StreamsComponent},
    {path: ':id', component: StreamEditorComponent},
];

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature('streams', StreamsReducer),
        EffectsModule.forFeature([StreamsEffects])
    ],
    declarations: [
        StreamsComponent,
        StreamEditorComponent,
        StreamDetailsComponent,
        StreamFilterComponent,
        BlocklyComponent,
        ParameterList,
        ParameterMap,
        ParameterComponent
    ],
    providers: []
})
export class StreamsModule { }