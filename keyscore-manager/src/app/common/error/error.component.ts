import {Component, Input} from "@angular/core";
import "./style/error.style.scss"

@Component({
    selector: "error-component",
    template: `
        <div fxFill fxLayout="column">
            <div class="top-column" fxLayoutAlign="center center" fxFlex="60">
                <a routerLink="/dashboard"
                   routerLinkActive="active">
                    <img src="/assets/images/logos/svg/dark/keyscore.dark.svg">
                </a>
            </div>
            <div class="bottom-column" fxLayoutAlign="start center" fxFlex="40" fxLayout="column">
                <div class="error-code">
                    <h1>{{httpError}}</h1>
                </div>
                <div class="error-message">
                    <h3>{{message}}</h3>
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
