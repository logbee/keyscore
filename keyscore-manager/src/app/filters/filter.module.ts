import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {LiveEditingComponent} from "./filter-details/live-editing.component";
import {EffectsModule} from "@ngrx/effects";
import {FilterEffects} from "./filters.effects";
import {FiltersComponent} from "./filters.component";
import {TranslateModule} from "@ngx-translate/core";

export const routes: Routes = [
    {path: '', component: FiltersComponent},
    {path: 'live-editing', component: LiveEditingComponent}
];

@NgModule({
    imports: [
        RouterModule.forChild(routes),
        // StoreModule.forFeature('filters', FilterReducer),
        EffectsModule.forFeature([FilterEffects]),
        TranslateModule
    ],

    declarations: [
        LiveEditingComponent,
        FiltersComponent,


    ],

    providers: []
})

export class FilterModule {}