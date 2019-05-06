import {Component, Input} from "@angular/core";
import {Health} from "keyscore-manager-models";

@Component({
    selector: "resource-health",
    template: `
        <div matTooltipPosition="above" matTooltip="{{health}}">
            <div class="health-light {{health}}"></div>
        </div>
    `,

})

export class ResourcesHealthComponent {
    @Input() public health: Health;
}