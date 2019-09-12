import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {AutocompleteFilterComponent} from "./autocomplete-filter.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {DurationInputComponent} from "./duration-input.component";
import {SliderInputComponent} from "./slider-input.component";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [AutocompleteFilterComponent, DurationInputComponent, SliderInputComponent],
    exports: [AutocompleteFilterComponent, DurationInputComponent, SliderInputComponent],
    entryComponents: [AutocompleteFilterComponent, DurationInputComponent, SliderInputComponent]
})
export class SharedControlsModule {
}