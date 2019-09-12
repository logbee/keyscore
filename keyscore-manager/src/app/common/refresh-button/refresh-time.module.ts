import {TranslateModule} from "@ngx-translate/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {RefreshTimeComponent} from "../refresh-button/refresh.component";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MaterialModule
    ],
    declarations: [RefreshTimeComponent],
    entryComponents: [RefreshTimeComponent],
    exports: [RefreshTimeComponent],
    providers: [],
})
export class RefreshTimeModule {

}
