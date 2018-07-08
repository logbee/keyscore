import {Component} from "@angular/core";

@Component({
    selector: "keyscore-dashboard",
    template: `
        <header-bar title="Dashboard"></header-bar>
        <div class="row justify-content-center">
            <div class="col-10">
                <h1>{{'GENERAL.DASHBOARD' | translate}}</h1>
            </div>
        </div>
    `
})

export class DashboardComponent {

}
