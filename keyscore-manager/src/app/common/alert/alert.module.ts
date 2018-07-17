import {NgModule} from "@angular/core";
import {TranslateModule} from "@ngx-translate/core";
import {AlertComponent} from "./alert.component";

@NgModule({
    imports: [TranslateModule],
    declarations: [AlertComponent],
    entryComponents: [AlertComponent],
    exports: [AlertComponent],
    providers: []
})
export class AlertModule {

}
