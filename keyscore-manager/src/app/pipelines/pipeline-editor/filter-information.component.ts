import {Component, Input} from "@angular/core";
import {FilterDescriptor, InternalPipelineConfiguration} from "../pipelines.model";

@Component({
    selector: "filter-information",
    template: `
        <div class="card">
            <div class="card-header" style="font-size: 22px">
                {{selectedFilter !== null ?
                    selectedFilter.displayName ? selectedFilter.displayName : selectedFilter.name :""}}
            </div>
            <div class="card-body">{{selectedFilter !== null ? selectedFilter.description : ""}}</div>
            <div class="card-footer"></div>
        </div>
    `
})

export class FilterInformationComponent {
    @Input() public selectedFilter: FilterDescriptor | InternalPipelineConfiguration;
}
