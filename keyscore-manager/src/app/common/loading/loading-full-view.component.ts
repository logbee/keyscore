import {Component} from "@angular/core";
import "./style/loading.style.scss"

@Component({
    selector: "loading-full-view",
    template: `
        <div fxLayout="column" fxFill class="loading-background">
            <div fxFlex="60" fxLayoutAlign="center center">
                <img src="/assets/images/logos/svg/dark/keyscore.dark.svg">
            </div>
            <div fxFlex="40" fxLayoutAlign="start center">
                <div class="spinner">
                    <div class="rect1"></div>
                    <div class="rect2"></div>
                    <div class="rect3"></div>
                    <div class="rect4"></div>
                    <div class="rect5"></div>
                </div>
            </div>
        </div>
    `,
})

export class LoadingFullViewComponent {

}
