import {Component, Input} from "@angular/core";
import {FilterStatus, Health} from "../../pipelines/pipelines.model";

@Component({
    selector: "status-light",
    template: `
        <div class="row float-right mr-1" title="Filter is {{status}}">
            <div class="status-light {{status}}"></div>
        </div>
    `,

})

export class StatuslightComponent {
    @Input() public status: FilterStatus;
}
