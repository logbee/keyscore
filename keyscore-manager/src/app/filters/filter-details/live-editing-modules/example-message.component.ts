import {Component} from "@angular/core";

@Component({
    selector: "example-message",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold">
                {{'FILTERLIVEEDITINGCOMPONENT.EXAMPLE_MESSAGE' | translate}}
            </div>
            <div class="card-body">
                <div class="form-group">
                                <textarea placeholder="{{'FILTERLIVEEDITINGCOMPONENT.MESSAGE_PLACEHOLDER' | translate}}"
                                          class="form-control" rows="5"></textarea>
                </div>

            </div>
        </div>
    `
})

export class ExampleMessageComponent {
}
