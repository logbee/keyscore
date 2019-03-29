import {NgModule} from "@angular/core";
import {HealthComponent} from "./health.component";
import {ResourcesHealthComponent} from "./resources-health.component";
import {MaterialModule} from "keyscore-manager-material";

@NgModule({
    declarations: [HealthComponent, ResourcesHealthComponent],
    entryComponents: [HealthComponent],
    imports: [MaterialModule],
    exports: [HealthComponent, ResourcesHealthComponent],
    providers: []
})
export class HealthModule {

}
