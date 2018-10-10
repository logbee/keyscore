import {TranslateModule} from "@ngx-translate/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {HeaderBarComponent} from "./headerbar.component";
import {MaterialModule} from "../../material.module";
import {RefreshTimeComponent} from "./refresh.component";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MaterialModule
    ],
    declarations: [HeaderBarComponent,RefreshTimeComponent],
    entryComponents: [HeaderBarComponent],
    exports: [HeaderBarComponent],
    providers: [],
})
export class HeaderBarModule {

}
