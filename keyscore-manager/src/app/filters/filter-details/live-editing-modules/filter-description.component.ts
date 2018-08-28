import {Component, Input} from "@angular/core";
import {FilterConfiguration} from "../../../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../../../models/filter-model/FilterInstanceState";

import "../../styles/filterstyle.css";

@Component({
    selector: "filter-description",
    template: `
        <mat-card>
            <mat-card-header>
                <mat-card-title class="font-weight-bold">{{currentFilter?.descriptor.displayName}}</mat-card-title>
            </mat-card-header>
            <mat-card-content>
                <mat-list role="list">
                    <mat-list-item role="listitem">{{'FILTERLIVEEDITINGCOMPONENT.DESCRIPTION' | translate}} :
                        {{currentFilter?.descriptor.description}}
                    </mat-list-item>
                    <mat-list-item role="listitem">{{'FILTERLIVEEDITINGCOMPONENT.CATEGORY' | translate}} :
                        {{currentFilter?.descriptor.category}}
                    </mat-list-item>
                    <mat-list-item role="listitem">{{'FILTERLIVEEDITINGCOMPONENT.THROUGHPUTTIME' | translate}}
                        {{currentFilter?.descriptor.displayName}} : {{currentFilterState?.throughPutTime / 1000000}} ms
                    </mat-list-item>
                    <mat-list-item role="listitem">{{'FILTERLIVEEDITINGCOMPONENT.TOTALTHROUGHPUTTIME' | translate}}
                        {{currentFilter?.descriptor.displayName}} : {{currentFilterState?.totalThroughputTime / 1000000}}
                    </mat-list-item>

                </mat-list>
                <!--<div class="row ml-3 mb-1">-->
                    <!--<p class="text-primary">{{currentFilter?.descriptor.description}}</p>-->
                <!--</div>-->
                <!--<div class="row ml-3 mb-1">-->
                    <!--<p class="text-muted">{{'FILTERLIVEEDITINGCOMPONENT.CATEGORY' | translate}}: </p>-->
                    <!--<p class="ml-1"> {{currentFilter?.descriptor.category}}</p>-->
                <!--</div>-->
                <!--<div class="row ml-3 mb-1">-->
                    <!--<p class="text-muted">{{'FILTERLIVEEDITINGCOMPONENT.THROUGHPUTTIME' | translate}}-->
                        <!--{{currentFilter?.descriptor.displayName}}:</p>-->
                    <!--<p class="ml-1"> {{currentFilterState?.throughPutTime / 1000000}}-->
                        <!--<small class="text-info"> ms</small>-->
                    <!--</p>-->
                <!--</div>-->
                <!--<div class="row ml-3 mb-1">-->
                    <!--<p class="text-muted">{{'FILTERLIVEEDITINGCOMPONENT.TOTALTHROUGHPUTTIME' | translate}}-->
                        <!--{{currentFilter?.descriptor.displayName}}: </p>-->
                    <!--<label class="ml-1"> {{currentFilterState?.totalThroughputTime / 1000000}}<p class="text-info">-->
                        <!--ms</p></label>-->
                <!--</div>-->
            </mat-card-content>
        </mat-card>
    `
})

export class FilterDescriptionComponent {

    @Input() public currentFilter: FilterConfiguration;
    @Input() public currentFilterState: FilterInstanceState;

    constructor() {
    }

}
