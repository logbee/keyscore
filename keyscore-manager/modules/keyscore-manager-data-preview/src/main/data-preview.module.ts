import {NgModule} from "@angular/core";
import {MaterialModule} from "@keyscore-manager-material/src/main/material.module";
import {TranslateModule} from "@ngx-translate/core";
import {CommonModule} from "@angular/common";
import {ValueType} from "./components/value.type.component";
import {DataPreviewComponent} from "./components/data-preview.component";
import {LeftToRightNavigationControl} from "./components/left-right-navigation-control.component";


@NgModule({
    imports: [
        MaterialModule,
        TranslateModule,
        CommonModule
    ],
    declarations: [DataPreviewComponent, LeftToRightNavigationControl, ValueType],
    entryComponents: [DataPreviewComponent],
    exports: [DataPreviewComponent, LeftToRightNavigationControl, ValueType],
    providers: []
})
export class DataPreviewModule{

}
