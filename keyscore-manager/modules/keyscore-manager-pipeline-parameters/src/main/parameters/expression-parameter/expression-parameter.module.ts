import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {TranslateModule} from "@ngx-translate/core";
import {MaterialModule} from "keyscore-manager-material";
import {ExpressionParameterComponent} from "./expression-parameter.component";


@NgModule({
    imports: [
        CommonModule,
        TranslateModule,
        MaterialModule,
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