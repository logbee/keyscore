import {NgModule} from "@angular/core";
import {MaterialModule} from "keyscore-manager-material";
import {TranslateModule} from "@ngx-translate/core";
import {CommonModule} from "@angular/common";
import {ViewPresets} from "./view.presets.component";
import {LeftToRightNavigationControl} from "./left-to-right-navigation-control.component";
import {ValueType} from "./value.type.component";
import {DatatableComponent} from "./datatable.component";


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
