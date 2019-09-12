import {TranslateModule} from "@ngx-translate/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {HeaderBarComponent} from "./headerbar.component";
import {RefreshTimeModule} from "../refresh-button/refresh-time.module";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MaterialModule,
        RefreshTimeModule
    ],
    declarations: [HeaderBarComponent],
    entryComponents: [HeaderBarComponent],
    exports: [HeaderBarComponent],
    providers: [],
})
export class HeaderBarModule {

}
