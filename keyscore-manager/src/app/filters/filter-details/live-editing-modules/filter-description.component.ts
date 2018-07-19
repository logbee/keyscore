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
                <div class="row">
                    <div class="col-11">
                        <div>
                            <!--<strong class="text-info">{{currentFilter.descriptor.description}}</strong>-->
                            <h6>Simple Filter that logs each dataset it receives.</h6>
                        </div>
                    </div>
                    <div class="col-1 text-right">
                        <status-light [status]="currentFilterState?.status"></status-light>
                    </div>
                </div>
                <div>
                    <strong class="text-muted">{{'FILTERLIVEEDITINGCOMPONENT.CATEGORY' | translate}}: </strong>
                    <strong> {{currentFilter?.descriptor.category}}</strong>
                </div>
                <div class="mt-1">
                    <strong class="text-muted">{{'FILTERLIVEEDITINGCOMPONENT.THROUGHPUTTIME' | translate}}: </strong>
                    <strong>{{currentFilterState?.throughPutTime}}</strong>
                </div>

            </div>
        </div>
    `
})

export class FilterDescriptionComponent {

    @Input() public currentFilter: FilterConfiguration;
    @Input() public currentFilterState: FilterInstanceState;

}
