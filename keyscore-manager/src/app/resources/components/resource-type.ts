import {Component, Input, OnInit} from "@angular/core";
import {BlueprintJsonClass} from "../../models/blueprints/Blueprint";
import {TranslateService} from "@ngx-translate/core";
import {MatIconRegistry} from "@angular/material";
import {DomSanitizer} from "@angular/platform-browser";

@Component({
    selector: "resource-type",
    template: `
        <ng-container [ngSwitch]="type">
            <div *ngSwitchCase="transform">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.FILTER' | translate}}">transform</mat-icon>
            </div>
            <div *ngSwitchCase="source">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.SOURCE' | translate}}">arrow_forward</mat-icon>
            </div>
            <div *ngSwitchCase="sink">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.SINK' | translate}}">arrow_back</mat-icon>

            </div>
            <div *ngSwitchCase="mergeing">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.MERGE' | translate}}">call_merge</mat-icon>
            </div>
            <div *ngSwitchCase="branching">
                <mat-icon matTooltipPosition="after" matTooltip="{{'GENERAL.BRANCH' | translate}}">call_split</mat-icon>
            </div>

        </ng-container>
       
    `
})

export class ResourceType {
    @Input() public type: string;

    constructor(private translate: TranslateService, private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer) {
      //TODO: Custom Icons for types
      //   this.matIconRegistry.addSvgIcon(
      //       'source',
      //               this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/chevron-left.svg")
      //   )
    }

    private transform: string = BlueprintJsonClass.FilterBlueprint;
    private source: string = BlueprintJsonClass.SourceBlueprint;
    private sink: string = BlueprintJsonClass.SinkBlueprint;
    private mergeing: string = BlueprintJsonClass.MergeBlueprint;
    private branching: string = BlueprintJsonClass.BranchBlueprint;


}