import {NgModule} from "@angular/core";
import {ErrorComponent} from "./error.component";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";

@NgModule({
    imports:[MaterialModule],
    declarations: [ErrorComponent],
    entryComponents: [ErrorComponent],
    exports: [ErrorComponent],
    providers: []
})
export class ErrorModule {

}
