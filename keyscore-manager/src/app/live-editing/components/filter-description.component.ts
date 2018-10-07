import {Component, Input} from "@angular/core";
import {Configuration} from "../../models/common/Configuration";
import {ResourceInstanceState} from "../../models/filter-model/ResourceInstanceState";
import "../filter-styles/filterstyle.css";
import {ResolvedFilterDescriptor} from "../../models/descriptors/FilterDescriptor";

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

                <mat-list fxFlexFill="" fxLayoutGap="15px" fxLayout="column" role="list">
                    <mat-list-item fxFlex="30%" fxFlexFill="" fxLayout="column" fxLayoutGap="15px" role="listitem">
                        <span fxFlex="30%"
                              class="mat-subheading-1 font-weight-bold">{{'FILTERLIVEEDITINGCOMPONENT.DESCRIPTION' | translate}}:
                        </span>
                        <span fxFlex="" class="mat-subheading-1">
                            {{descriptor?.description}}
                        </span>
                    </mat-list-item>
                    <mat-list-item fxFlex="30%" fxFlexFill="" fxLayout="column" fxLayoutGap="15px" role="listitem">
                        <span fxFlex="30%"
                              class="mat-subheading-1 font-weight-bold">{{'FILTERLIVEEDITINGCOMPONENT.THROUGHPUTTIME' | translate}}
                            {{descriptor?.displayName}}: 
                        </span>
                        <span fxFlex="" class="mat-subheading-1">
                            {{currentFilterState?.throughPutTime / 1000000}} ms
                        </span>
                    </mat-list-item>
                    <mat-list-item fxFlex="" fxFlexFill="" fxLayout="column" fxLayoutGap="15px" role="listitem">
                        <span fxFlex="30%" class="mat-subheading-1 font-weight-bold">
                            {{'FILTERLIVEEDITINGCOMPONENT.TOTALTHROUGHPUTTIME' | translate}} {{descriptor?.displayName}}:
                        </span>
                        <span  fxFlex="" class="mat-subheading-1">
                            {{currentFilterState?.totalThroughputTime / 1000000}} ms
                        </span>
                    </mat-list-item>

                </mat-list>
            </mat-card-content>
        </mat-card>
    `
})

export class FilterDescriptionComponent {

    @Input() public currentFilter: Configuration;
    @Input() public currentFilterState: ResourceInstanceState;
    @Input() public descriptor: ResolvedFilterDescriptor;

    constructor() {
    }

}
