import {Component, Input} from "@angular/core";
import {ResolvedFilterDescriptor} from "../../models/descriptors/FilterDescriptor";
import {Configuration} from "../../models/common/Configuration";

@Component({
    selector: "json-visualizer",
    template: `
        <mat-tab-group fxFlexFill="" mat-stretch-tabs dynamicHeight>
            <mat-tab label="Descriptor" fxFlexFill="" fxLayoutGap="15px">
                <div fxFlexFill="" fxLayoutGap="15px">
                    <ngx-json-viewer fxFlex="90%" [json]="descriptor"></ngx-json-viewer>
                    <button matTooltipPosition="after" matTooltip="Copy Json" mat-icon-button fxFlex="10%" (click)="copyDesc()">
                        <mat-icon>content_copy</mat-icon>
                    </button>
                </div>
            </mat-tab>
            <mat-tab label="Configuration">
                <div fxFlexFill="" fxLayoutGap="15px">
                    <ngx-json-viewer fxFlex="90%" [json]="descriptor"></ngx-json-viewer>
                    <button  mat-icon-button fxFlex="10%" (click)="copyConfig(des)">
                        <mat-icon>content_copy</mat-icon>
                    </button>
                </div>
            </mat-tab>
        </mat-tab-group>
    `
})

export class JsonVisualizer {
    @Input() descriptor: ResolvedFilterDescriptor;
    @Input() configuration: Configuration;

    copyDesc(){
        let val = JSON.stringify(this.descriptor);
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

    copyConfig() {
        let val = JSON.stringify(this.configuration);
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