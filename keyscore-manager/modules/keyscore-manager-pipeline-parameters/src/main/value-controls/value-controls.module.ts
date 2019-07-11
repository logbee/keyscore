import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MaterialModule} from "keyscore-manager-material";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BooleanValueComponent} from "./boolean-value.component";
import {TextValueComponent} from "./text-value.component";
import {TimestampValueComponent} from "./timestamp-value.component";
import {ValueDirective} from "./directives/value.directive";
import {DurationValueComponent} from "./duration-value.component";
import {SharedControlsModule} from "../shared-controls/shared-controls.module";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule,
        ReactiveFormsModule,
        FormsModule,
        SharedControlsModule
    ],
    declarations: [
        ValueDirective,
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent,
        DurationValueComponent
    ],
    exports: [
        ValueDirective,
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent,
        DurationValueComponent
    ],
    entryComponents:[
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent,
        DurationValueComponent
    ]
})
export class ValueControlsModule {
}