import {Component, Input} from "@angular/core";

@Component({
    selector: "error-component",
    template: `
        <div class="row pb-3"
             style="background-color: rgba(0,0,0,0.69); max-height: 40vh; min-height: 40vh; margin-top: -20px">
            <div class="col text-center">
                <img class="mt-3" src="/assets/images/logos/keyscore.dark.svg"
                     style="height:38vh; width: 100vw">
            </div>
        </div>
        <div class="row" style="min-height: 54.9vh; background-color: rgba(0,0,0,0.35)">
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
    @Input() public httpError: Response;
    @Input() public message: string;
}
