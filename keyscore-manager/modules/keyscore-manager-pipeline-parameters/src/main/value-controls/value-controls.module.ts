import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MaterialModule} from "keyscore-manager-material";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BooleanValueComponent} from "./boolean-value.component";
import {TextValueComponent} from "./text-value.component";
import {TimestampValueComponent} from "./timestamp-value.component";
import {ValueDirective} from "./directives/value.directive";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule,
        ReactiveFormsModule,
        FormsModule
    ],
    declarations: [
        ValueDirective,
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent
    ],
    exports: [
        ValueDirective,
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent
    ],
    entryComponents:[
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent
    ]
})
export class ValueControlsModule {
}