import {Component, Input} from "@angular/core";
import {FilterStatus} from "../../models/filter-model/FilterStatus";
import {RestCallService} from "../../services/rest-api/rest-call.service";

@Component({
    selector: "status-light",
    template: `
        <div matTooltipPosition="above" matTooltip="Health">
            <div class="status-light {{status}}"></div>
        </div>
    `,

})

export class StatuslightComponent {
    @Input() public uuid: string;

    constructor(private restcallService: RestCallService) {
    }
}