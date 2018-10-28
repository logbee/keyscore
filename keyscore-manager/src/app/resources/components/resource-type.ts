import {Component, Input} from "@angular/core";
import {BlueprintJsonClass} from "../../models/blueprints/Blueprint";
import {MatIconRegistry} from "@angular/material";
import {DomSanitizer} from "@angular/platform-browser";

@Component({
    selector: "resource-type",
    template: `
        <ng-container [ngSwitch]="type">
            <div *ngSwitchCase="transform">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.FILTER' | translate}}" svgIcon="filter-block"></mat-icon>
            </div>
            <div *ngSwitchCase="source">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.SOURCE' | translate}}" svgIcon="source-block"></mat-icon>
            </div>
            <div *ngSwitchCase="sink">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.SINK' | translate}}" svgIcon="sink-block"></mat-icon>

            </div>
            <div *ngSwitchCase="merging">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.MERGE' | translate}}" svgIcon="merge-block"></mat-icon>
            </div>
            <div *ngSwitchCase="branching">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.BRANCH' | translate}}" svgIcon="branch-block"></mat-icon>
            </div>
        </ng-container>
       
    `
})

export class ResourceType {
    @Input() public type: string;
    private transform: string = BlueprintJsonClass.FilterBlueprint;
    private source: string = BlueprintJsonClass.SourceBlueprint;
    private sink: string = BlueprintJsonClass.SinkBlueprint;
    private merging: string = BlueprintJsonClass.MergeBlueprint;
    private branching: string = BlueprintJsonClass.BranchBlueprint;
}