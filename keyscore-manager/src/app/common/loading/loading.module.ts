import {NgModule} from "@angular/core";
import {LoadingComponent} from "./loading.component";
import {LoadingFullViewComponent} from "./loading-full-view.component";
import {MaterialModule} from "@keyscore-manager-material";

@NgModule({
    imports: [MaterialModule],
    declarations: [LoadingComponent, LoadingFullViewComponent],
    entryComponents: [LoadingComponent, LoadingFullViewComponent],
    exports: [LoadingComponent, LoadingFullViewComponent],
    providers: []
})
export class LoadingModule {

}
