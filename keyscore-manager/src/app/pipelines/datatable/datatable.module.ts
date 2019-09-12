import {NgModule} from "@angular/core";
import {TranslateModule} from "@ngx-translate/core";
import {CommonModule} from "@angular/common";
import {ViewPresets} from "./view.presets.component";
import {LeftToRightNavigationControl} from "./left-to-right-navigation-control.component";
import {ValueType} from "./value.type.component";
import {DatatableComponent} from "./datatable.component";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";


@NgModule({
    imports: [
        MaterialModule,
        TranslateModule,
        CommonModule
    ],
    declarations: [DatatableComponent, ViewPresets, LeftToRightNavigationControl, ValueType],
    entryComponents: [DatatableComponent],
    exports: [DatatableComponent],
    providers: []
})
export class DatatableModule{

}
