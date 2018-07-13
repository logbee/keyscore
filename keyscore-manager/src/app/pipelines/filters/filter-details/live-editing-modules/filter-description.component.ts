import {Component, Input, OnInit} from "@angular/core";
import {FilterConfiguration, FilterInstanceState, FilterState} from "../../../pipelines.model";
import {TranslateService} from "@ngx-translate/core";
import {Store} from "@ngrx/store";

@Component({
    selector: "filter-description",
    template: `
        <div class="card">
            <div class="card-header alert-light font-weight-bold">
                {{'FILTERLIVEEDITINGCOMPONENT.FILTERDESCRIPTION_TITLE' | translate}}
            </div>
            <div class="card-body">
                <table class="table table-condensed">
                    <thead>
                    <tr>
                        <th> {{'FILTERLIVEEDITINGCOMPONENT.ID' | translate}}</th>
                        <th> {{'FILTERLIVEEDITINGCOMPONENT.NAME' | translate}}</th>
                        <th> {{'FILTERLIVEEDITINGCOMPONENT.DESCRIPTION' | translate}}</th>
                        <th> {{'FILTERLIVEEDITINGCOMPONENT.THROUGHPUTTIME' | translate}}</th>
                        <th> {{'FILTERLIVEEDITINGCOMPONENT.TOTALTHROUGHPUTTIME' | translate}}</th>
                        <th> {{'FILTERLIVEEDITINGCOMPONENT.STATUS' | translate}}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>{{ currentFilter?.id}}</td>
                        <td>{{ currentFilter?.descriptor?.displayName}}</td>
                        <td>{{ currentFilter?.descriptor?.description}}</td>
                        <td>{{ currentFilterState?.throughPutTime}}</td>
                        <td>{{ currentFilterState?.totalThroughputTime}}</td>
                        <td>{{ currentFilterState?.status}}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    `
})

export class FilterDescriptionComponent implements OnInit{

    @Input() public currentFilter: FilterConfiguration;
    @Input() public currentFilterState: FilterInstanceState;

    constructor() {
    }

    ngOnInit(): void {
        console.log("[filter-description]" + this.currentFilterState.totalThroughputTime);
    }

}
