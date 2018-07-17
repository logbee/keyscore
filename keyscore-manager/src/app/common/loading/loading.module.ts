import {NgModule} from "@angular/core";
import {LoadingComponent} from "./loading.component";
import {LoadingFullViewComponent} from "./loading-full-view.component";

@NgModule({
    declarations: [LoadingComponent, LoadingFullViewComponent],
    entryComponents: [LoadingComponent, LoadingFullViewComponent],
    exports: [LoadingComponent, LoadingFullViewComponent],
    providers: []
})
export class LoadingModule {

}
