import {Component, Input, OnInit} from "@angular/core";
import {Store} from "@ngrx/store";
import {Health} from "../../models/common/Health";

@Component({
    selector: "resource-health",
    template: `
        <div matTooltipPosition="above" matTooltip="Health">
            <div class="health-light {{health}}"></div>
        </div>
    `,

})

export class ResourcesHealthComponent implements OnInit {
    @Input() public health: Health;

    constructor(private store: Store<any>) {
    }

    ngOnInit(): void {
    }
}