import {Component, Input} from "@angular/core";
import {BlueprintJsonClass} from "keyscore-manager-models";

@Component({
    selector: "stage-type",
    template: `
        <ng-container [ngSwitch]="stageType">
            <div *ngSwitchCase="transform">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.FILTER' | translate}}"
                          svgIcon="filter-stage"></mat-icon>
            </div>
            <div *ngSwitchCase="source">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.SOURCE' | translate}}"
                          svgIcon="source-stage"></mat-icon>
            </div>
            <div *ngSwitchCase="sink">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.SINK' | translate}}"
                          svgIcon="sink-stage"></mat-icon>

            </div>
            <div *ngSwitchCase="merging">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.MERGE' | translate}}"
                          svgIcon="merge-block"></mat-icon>
            </div>
            <div *ngSwitchCase="branching">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.BRANCH' | translate}}"
                          svgIcon="branch-block"></mat-icon>
            </div>
        </ng-container>

    `
})

export class StageType {
    @Input() public stageType: string;
    private transform: string = BlueprintJsonClass.FilterBlueprint;
    private source: string = BlueprintJsonClass.SourceBlueprint;
    private sink: string = BlueprintJsonClass.SinkBlueprint;
    private merging: string = BlueprintJsonClass.MergeBlueprint;
    private branching: string = BlueprintJsonClass.BranchBlueprint;
}