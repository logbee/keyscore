import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MaterialModule} from "keyscore-manager-material";
import {ExpressionParameterComponent} from "./expression-parameter.component";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule
    ],
    declarations: [
        ExpressionParameterComponent
    ],
    exports: [
    ],
    providers: [
    ]
})
export class ExpressionParameterModule {

}