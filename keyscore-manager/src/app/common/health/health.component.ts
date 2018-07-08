import {Component, Input, OnDestroy} from "@angular/core";
import {Health} from "../../pipelines/pipelines.model";
import {Observable} from "rxjs/internal/Observable";
import {takeWhile} from "rxjs/operators";

@Component({
    selector: "health-light",
    template: `
        <div class="row justify-content-around">
            <div class="health-light-Red {{health}}"></div>
            <div class="health-light-Yellow {{health}}"></div>
            <div class="health-light-Green {{health}}"></div>
        </div>
    `,

})

export class HealthComponent {
    @Input() public health: Health;
}
