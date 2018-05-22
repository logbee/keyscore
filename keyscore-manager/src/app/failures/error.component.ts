import {Component, Input} from "@angular/core";

@Component({
    selector: 'error-component',
    template: `
        <div class="justify-content-center">
            <span>{{httpError}} : {{message}}</span>
        </div>
    `
})

export class ErrorComponent {
    @Input() httpError: Response;
    @Input() message: String;

    constructor() {

    }
}