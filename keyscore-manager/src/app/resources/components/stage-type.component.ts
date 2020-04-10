import {Component, Input} from "@angular/core";
import {BlueprintJsonClass} from "@/../modules/keyscore-manager-models/src/main/blueprints/Blueprint";

@Component({
    selector: "stage-type",
    template: `
        <ng-container [ngSwitch]="stageType">
            <div *ngSwitchCase="blueprintJsonClass.FilterBlueprint">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.FILTER' | translate}}"
                          svgIcon="filter-stage"></mat-icon>
            </div>
            <div *ngSwitchCase="blueprintJsonClass.SourceBlueprint">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.SOURCE' | translate}}"
                          svgIcon="source-stage"></mat-icon>
            </div>
            <div *ngSwitchCase="blueprintJsonClass.SinkBlueprint">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.SINK' | translate}}"
                          svgIcon="sink-stage"></mat-icon>

            </div>
            <div *ngSwitchCase="blueprintJsonClass.MergeBlueprint">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.MERGE' | translate}}"
                          svgIcon="merge-block"></mat-icon>
            </div>
            <div *ngSwitchCase="blueprintJsonClass.BranchBlueprint">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.BRANCH' | translate}}"
                          svgIcon="branch-block"></mat-icon>
            </div>
        </ng-container>

    `
})

export class StageType {
    blueprintJsonClass: typeof BlueprintJsonClass = BlueprintJsonClass;

    @Input() stageType: string;
}
