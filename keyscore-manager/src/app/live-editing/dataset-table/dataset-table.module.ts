import {NgModule} from "@angular/core";
import {MaterialModule} from "../../material.module";
import {DatasetTable} from "./dataset-table";
import {ViewPresets} from "./view-presets.component";
import {LeftToRightNavigationControl} from "./left-to-right-navigation-control.component";
import {TopToBottomNavigationControl} from "./topToBottom-navigation-control";
import {ValueType} from "./value-type";
import {TranslateModule} from "@ngx-translate/core";
import {CommonModule} from "@angular/common";

@NgModule({
    imports: [
        MaterialModule,
        TranslateModule,
        CommonModule
    ],
    declarations: [DatasetTable, ViewPresets, LeftToRightNavigationControl, TopToBottomNavigationControl, ValueType],
    entryComponents: [DatasetTable],
    exports: [DatasetTable],
    providers: []
})
export class DatasetTableModule {

}
