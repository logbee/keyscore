import {Component} from "@angular/core";

@Component({
    selector: "loading",
    template: `
        <div class="spinner-dark">
            <div class="rect1"></div>
            <div class="rect2"></div>
            <div class="rect3"></div>
            <div class="rect4"></div>
            <div class="rect5"></div>
        </div>
    `
})

export class LoadingComponent {

}
