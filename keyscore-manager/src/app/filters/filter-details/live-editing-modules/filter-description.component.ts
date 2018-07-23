import {Component, Input} from "@angular/core";
import {FilterConfiguration} from "../../../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../../../models/filter-model/FilterInstanceState";

@Component({
    selector: "filter-description",
    template: `
        <div class="card">
            <div class="card-header font-weight-bold d-flex justify-content-between"
                 style="background-color: #3a88b3; color: white">
                <h4>{{currentFilter?.descriptor.displayName}}</h4>
            </div>
            <div class="card-body">
                <div class="row ml-3 mb-1">
                        <small class="text-primary">{{currentFilter?.descriptor.description}}</small>
                </div>
                <div class="row ml-3 mb-1">
                    <strong class="text-muted">{{'FILTERLIVEEDITINGCOMPONENT.CATEGORY' | translate}}: </strong>
                    <strong> {{currentFilter?.descriptor.category}}</strong>
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
