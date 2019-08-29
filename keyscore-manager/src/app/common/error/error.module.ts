import {NgModule} from "@angular/core";
import {ErrorComponent} from "./error.component";
import {MaterialModule} from "@keyscore-manager-material";

@NgModule({
    imports:[MaterialModule],
    declarations: [ErrorComponent],
    entryComponents: [ErrorComponent],
    exports: [ErrorComponent],
    providers: []
})
export class ErrorModule {

}
