import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MaterialModule} from "keyscore-manager-material";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BooleanValueComponent} from "./boolean-value.component";
import {TextValueComponent} from "./text-value.component";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [BooleanValueComponent, TextValueComponent],
    exports: [BooleanValueComponent, TextValueComponent]
})
export class ValueControlsModule {
}