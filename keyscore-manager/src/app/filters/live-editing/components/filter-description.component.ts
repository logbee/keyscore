import {Component, Input} from "@angular/core";
import {Configuration} from "../../../models/common/Configuration";
import {ResourceInstanceState} from "../../../models/filter-model/ResourceInstanceState";
import "../filter-styles/filterstyle.css";

@Component({
    selector: "filter-description",
    template: `
        <mat-card>
            <mat-card-header class="fix-div">
                <div class="container" fxFlexFill="" fxLayout="row" fxLayout.xs="column">
                    <div fxFlexAlign="start" fxFlex="100%">
                        <mat-card-title>
                            <h1 class="mat-headline font-weight-bold">{{currentFilter?.descriptor.displayName}}</h1>
                        </mat-card-title>
                    </div>
                </div>
            </mat-card-header>
            <mat-card-content>

                <mat-list role="list">
                    <mat-list-item role="listitem"><span
                            class="mat-subheading-1 font-weight-bold">{{'FILTERLIVEEDITINGCOMPONENT.DESCRIPTION' | translate}}</span>
                        <span class="mat-subheading-1">{{currentFilter?.descriptor.description}}</span>
                    </mat-list-item>
                    <mat-list-item role="listitem"><span
                            class="mat-subheading-1 font-weight-bold">{{'FILTERLIVEEDITINGCOMPONENT.CATEGORY' | translate}}</span>
                        <span class="mat-subheading-1">{{currentFilter?.descriptor.categories}}</span>
                    </mat-list-item>
                    <mat-list-item role="listitem"><span
                            class="mat-subheading-1 font-weight-bold">{{'FILTERLIVEEDITINGCOMPONENT.THROUGHPUTTIME' | translate}}
                        {{currentFilter?.descriptor.displayName}} : </span><span
                            class="mat-subheading-1">{{currentFilterState?.throughPutTime / 1000000}} ms</span>
                    </mat-list-item>
                    <mat-list-item role="listitem"><span
                            class="mat-subheading-1 font-weight-bold">{{'FILTERLIVEEDITINGCOMPONENT.TOTALTHROUGHPUTTIME' | translate}} 
                       {{currentFilter?.descriptor.displayName}}  : </span><span
                            class="mat-subheading-1">{{currentFilterState?.totalThroughputTime / 1000000}}</span>
                    </mat-list-item>

                </mat-list>
            </mat-card-content>
        </mat-card>
    `
})

export class FilterDescriptionComponent {

    @Input() public currentFilter: Configuration;
    @Input() public currentFilterState: ResourceInstanceState;

    constructor() {
    }

}
