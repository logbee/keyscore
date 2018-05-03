import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {LiveEditingComponent} from "./filter-details/live-editing.component";
import {StoreModule} from "@ngrx/store";
import {EffectsModule} from "@ngrx/effects";
import {FilterEffects} from "./filters.effects";
import {FiltersComponent} from "./filters.component";
import {FilterDetailComponent} from "./filter-detail.component";

export const routes: Routes = [
    {path: '', component: FiltersComponent},
    {path: 'live-editing', component: LiveEditingComponent}
];

@NgModule({
    imports: [
        RouterModule.forChild(routes),
        // StoreModule.forFeature('filters', FilterReducer),
        EffectsModule.forFeature([FilterEffects])
    ],

    declarations: [
        LiveEditingComponent,
        FiltersComponent

    ],

    providers: []
})

export class FilterModule {}