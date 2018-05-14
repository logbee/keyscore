import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {StreamsComponent} from "./streams.component";
import {CommonModule} from "@angular/common";
import {StreamEditorComponent} from "./stream-editor/stream-editor.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {StreamsReducer} from "./streams.reducer";
import {ActionReducerMap, StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {StreamsEffects} from "./streams.effects";
import {StreamDetailsComponent} from "./stream-editor/stream-details.component";
import {StreamFilterComponent} from "./stream-editor/stream-filter.component";
import {ParameterList} from "./stream-editor/filter-editor/parameter-list.component";
import {ParameterComponent} from "./stream-editor/filter-editor/parameter.component";
import {ParameterMap} from "./stream-editor/filter-editor/parameter-map.component";
import {BlocklyComponent} from "./stream-editor/blockly/blockly.component";
import {TranslateModule} from "@ngx-translate/core";
import {FilterEffects} from "./filters/filters.effects";
import {FilterReducer} from "./filters/filter.reducer";
import {FiltersComponent} from "./filters/filters.component";
import {LiveEditingComponent} from "./filters/filter-details/live-editing.component";
import {StreamsModuleState} from "./streams.model";


export const routes: Routes = [
    {path: 'stream', component: StreamsComponent},
    {path: 'stream/:id', component: StreamEditorComponent},
    {path: 'filter', component: FiltersComponent},
    {path: 'filter/:id', component: LiveEditingComponent}
];

export const reducers: ActionReducerMap<StreamsModuleState> = {
    streams: StreamsReducer,
    filter: FilterReducer
};

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild(routes),
        StoreModule.forFeature('streams', reducers),
        EffectsModule.forFeature([StreamsEffects, FilterEffects]),
        TranslateModule
    ],
    declarations: [
        StreamsComponent,
        StreamEditorComponent,
        StreamDetailsComponent,
        StreamFilterComponent,
        BlocklyComponent,
        ParameterList,
        ParameterMap,
        ParameterComponent,
        LiveEditingComponent,
        FiltersComponent
    ],
    providers: []
})
export class StreamsModule {
}