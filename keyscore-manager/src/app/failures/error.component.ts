import {Component, Input} from "@angular/core";

@Component({
    selector: 'error-component',
    template: `
        <div class="row" style="background-color: rgba(0,0,0,0.35); min-height: 40vh; margin-top: -20px">
            <div class="col-sm-4"></div>
            <div class="col-sm-6">
                <img src="/assets/images/logos/keyscore.dark.svg"
                     class="img-responsive justify-content-center m-3">
            </div>
            <div class="col-sm-2"></div>


        </div>
        <div class="row" style="min-height: 54.9vh; background-color: rgba(0,0,0,0.69)">
            <div class="col align-self-center" >
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
    @Input() httpError: Response;
    @Input() message: String;

    constructor() {

    }
}