import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MaterialModule} from "@keyscore-manager-material";
import {AutocompleteFilterComponent} from "./autocomplete-filter.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {DurationInputComponent} from "./duration-input.component";
import {SliderInputComponent} from "./slider-input.component";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [AutocompleteFilterComponent, DurationInputComponent, SliderInputComponent],
    exports: [AutocompleteFilterComponent, DurationInputComponent, SliderInputComponent],
    entryComponents: [AutocompleteFilterComponent, DurationInputComponent, SliderInputComponent]
})
export class SharedControlsModule {
}