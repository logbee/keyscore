import {NgModule} from "@angular/core";
import {HealthComponent} from "./health.component";

@NgModule({
    declarations: [HealthComponent],
    entryComponents: [HealthComponent],
    exports: [HealthComponent],
    providers: []
})
export class HealthModule {

}
