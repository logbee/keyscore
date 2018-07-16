import {Component, Input, OnInit} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {Store} from "@ngrx/store";
import {FilterConfiguration} from "../../../../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../../../../models/filter-model/FilterInstanceState";

@Component({
    selector: "filter-description",
    template: `
        <div class="card">
            <div class="card-header font-weight-bold d-flex justify-content-between"
                 style="background-color: #3a88b3; color: white">
                <h4>{{currentFilter.descriptor.displayName}}</h4>
                <span class="mr-3">
                    <health-light [health]="this.currentFilterState.health"></health-light>
                </span>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-11">
                        <div>
                            <!--<small class="text-info">{{currentFilter.descriptor.description}}</small>-->
                            <small>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam.</small>
                        </div>
                    </div>
                    <div class="col-1 text-right">
                        <status-light [status]="currentFilterState.status"></status-light>
                    </div>
                </div>
                <div>
                    <small>{{'FILTERLIVEEDITINGCOMPONENT.CATEGORY' | translate}}: </small>
                    <small class="text-info"> {{currentFilter.descriptor.category}}</small>
                </div>
                <div>
                    <small>{{currentFilterState.throughPutTime}}</small>
                </div>

            </div>
        </div>
    `
})

export class FilterDescriptionComponent implements OnInit {

    @Input() public currentFilter: FilterConfiguration;
    @Input() public currentFilterState: FilterInstanceState;

    public ngOnInit(): void {
        // console.log("[filter-description]" + this.currentFilterState.totalThroughputTime);
    }

}
