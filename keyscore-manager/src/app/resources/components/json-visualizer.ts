import {Component, Input} from "@angular/core";
import {Store} from "@ngrx/store";
import {Configuration} from "@/../modules/keyscore-manager-models/src/main/common/Configuration";
import {FilterDescriptor} from "@/../modules/keyscore-manager-models/src/main/descriptors/FilterDescriptor";

@Component({
    selector: "json-visualizer",
    template: `
        <mat-tab-group fxFlexFill="" mat-stretch-tabs dynamicHeight>
            <mat-tab label="Descriptor" fxFlexFill="" fxLayoutGap="15px">
                <div fxFlexFill="" fxLayoutGap="15px">
                    <ngx-json-viewer fxFlex="90%" [json]="descriptor"></ngx-json-viewer>
                    <button matTooltipPosition="after" matTooltip="Copy Json" mat-icon-button fxFlex="10%"
                            (click)="copy('descriptor')">
                        <mat-icon>content_copy</mat-icon>
                    </button>
                </div>
            </mat-tab>
            <mat-tab label="Configuration">
                <div fxFlexFill="" fxLayoutGap="15px">
                    <ngx-json-viewer fxFlex="90%" [json]="configuration"></ngx-json-viewer>
                    <button matTooltipPosition="after" matTooltip="Copy Json" mat-icon-button fxFlex="10%" 
                            (click)="copy('configuration')">
                        <mat-icon>content_copy</mat-icon>
                    </button>
                </div>
            </mat-tab>
        </mat-tab-group>
    `
})

export class JsonVisualizer {
    @Input() private descriptor: FilterDescriptor;
    @Input()private configuration: Configuration;

    constructor() {
    }

    copy(which: string) {
        let copiedElement:any;
        if (which == "descriptor") {
            copiedElement = this.descriptor
        } else if(which == "configuration")  {
            copiedElement = this.configuration;
        }
        let val = JSON.stringify(copiedElement);
        let selBox = document.createElement('textarea');
        selBox.style.position = 'fixed';
        selBox.style.left = '0';
        selBox.style.top = '0';
        selBox.style.opacity = '0';
        selBox.value = val;
        document.body.appendChild(selBox);
        selBox.focus();
        selBox.select();
        document.execCommand('copy');
        document.body.removeChild(selBox);
    }
}