import {Component, Input} from "@angular/core";
import {FilterDescriptor} from "../../models/filter-model/FilterDescriptor";
import {InternalPipelineConfiguration} from "../../models/pipeline-model/InternalPipelineConfiguration";

@Component({
    selector: "filter-information",
    template: `
        <div class="card">
            <div class="card-header" style="font-size: 22px">
                {{selectedFilter?.displayName ? selectedFilter?.displayName : selectedFilter?.name}}
            </div>
            <div class="card-body">{{selectedFilter?.description}}</div>
            <div class="card-footer"></div>
        </div>
    `
})

export class FilterInformationComponent {
    @Input() public selectedFilter: FilterDescriptor | InternalPipelineConfiguration;
}
