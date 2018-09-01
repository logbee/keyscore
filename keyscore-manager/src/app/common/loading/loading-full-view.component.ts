import {Component} from "@angular/core";

@Component({
    selector: "loading-full-view",
    template: `
    <div class="col-12">
        <div class="loading-background">
            <div class="row justify-content-center">
                <div class="col-12 justify-content-center d-flex mt-5">
                    <img src="/assets/images/logos/svg/dark/keyscore.dark.svg">
                </div>
            </div>
            <div class="row">
                <div class="col-12 align-self-center">
                    <div class="spinner">
                        <div class="rect1"></div>
                        <div class="rect2"></div>
                        <div class="rect3"></div>
                        <div class="rect4"></div>
                        <div class="rect5"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    `,
})

export class LoadingFullViewComponent {

}
