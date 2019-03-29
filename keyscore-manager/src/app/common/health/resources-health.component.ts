import {Component, Input} from "@angular/core";
import {Health} from "../../../../modules/keyscore-manager-models/src/main/common/Health";

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