import {TranslateModule} from "@ngx-translate/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {HeaderBarComponent} from "./headerbar.component";
import {MaterialModule} from "@keyscore-manager-material";
import {RefreshTimeModule} from "../refresh-button/refresh-time.module";

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
