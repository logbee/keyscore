import {Component, Input} from "@angular/core";
import {FilterConfiguration} from "../../../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../../../models/filter-model/FilterInstanceState";

import "../../styles/filterstyle.css";

@Component({
    selector: "filter-description",
    template: `
        <div class="card">
            <div id="custom-card-white">
                <div class="header-background card-header card-header-background font-weight-bold d-flex justify-content-between">
                    <h4>{{currentFilter?.descriptor.displayName}}</h4>
                </div>
            </div>
            <div class="card-body">
                <div class="row ml-3 mb-1">
                    <small class="text-primary">{{currentFilter?.descriptor.description}}</small>
                </div>
                <div class="row ml-3 mb-1">
                    <strong class="text-muted">{{'FILTERLIVEEDITINGCOMPONENT.CATEGORY' | translate}}: </strong>
                    <strong class="ml-1"> {{currentFilter?.descriptor.category}}</strong>
                </div>
                <div class="row ml-3 mb-1">
                    <strong class="text-muted">{{'FILTERLIVEEDITINGCOMPONENT.THROUGHPUTTIME' | translate}}
                        {{currentFilter?.descriptor.displayName}}:</strong>
                    <strong class="ml-1"> {{currentFilterState?.throughPutTime / 1000000}}
                        <small class="text-info"> ms</small>
                    </strong>
                </div>
                <div class="row ml-3 mb-1">
                    <strong class="text-muted">{{'FILTERLIVEEDITINGCOMPONENT.TOTALTHROUGHPUTTIME' | translate}}
                        {{currentFilter?.descriptor.displayName}}: </strong>
                    <strong class="ml-1"> {{currentFilterState?.totalThroughputTime / 1000000}}
                        <small class="text-info"> ms</small>
                    </strong>
                </div>

            </div>
        </div>
    `
})

export class FilterDescriptionComponent {

    @Input() public currentFilter: FilterConfiguration;
    @Input() public currentFilterState: FilterInstanceState;

    constructor() {
    }

}
