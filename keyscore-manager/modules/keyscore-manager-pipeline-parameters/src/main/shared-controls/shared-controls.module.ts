import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MaterialModule} from "keyscore-manager-material";
import {AutocompleteFilterComponent} from "./autocomplete-filter.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [AutocompleteFilterComponent],
    exports: [AutocompleteFilterComponent]
})
export class SharedControlsModule {
}