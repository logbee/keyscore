import {Component, Input} from "@angular/core";
@Component({
    selector: "error-component",
    template: `
        <div class="row pb-3"
             style="background-color: #365078; max-height: 50vh; min-height: 50vh;">
            <div class="col text-center">
                <img class="mt-lg-4 pr-lg-5" src="/assets/images/logos/keyscore.dark.svg"
                     style="height:40vh; width: 100vw">
            </div>
        </div>
        <div class="row" style="min-height: 50vh; background-color: #365880">
            <div class="col align-self-center">
                <div class="row justify-content-center">
                    <h1 style="color: white; font-size: 6em">{{httpError}}</h1>
                </div>
                <div class="row justify-content-center">
                    <h3 style="color: white; font-size: 4em">{{message}}</h3>
                </div>
            </div>
        </div>


    `
})
export class ErrorComponent {
    @Input() public httpError?: string;
    @Input() public message?: string;

    constructor() {
        if (this.httpError == null || this.message == null) {
            this.message = "Something went wrong!";
            this.httpError = "Ups";
        }
    }
}
