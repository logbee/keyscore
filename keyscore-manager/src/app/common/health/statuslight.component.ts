import {Component, Input} from "@angular/core";
import {FilterStatus} from "../../models/filter-model/FilterStatus";

@Component({
    selector: "status-light",
    template: `
        <div matTooltipPosition="above" matTooltip="Filter is {{status}}">
            <div class="status-light {{status}}"></div>
        </div>
    `,

})

export class StatuslightComponent {
    @Input() public status: FilterStatus;
}