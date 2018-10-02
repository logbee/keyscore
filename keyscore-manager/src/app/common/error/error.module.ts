import {NgModule} from "@angular/core";
import {ErrorComponent} from "./error.component";
import {MaterialModule} from "../../material.module";

@NgModule({
    imports:[MaterialModule],
    declarations: [ErrorComponent],
    entryComponents: [ErrorComponent],
    exports: [ErrorComponent],
    providers: []
})
export class ErrorModule {

}
