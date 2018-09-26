import {Component, Input} from "@angular/core";
import {ResolvedFilterDescriptor} from "../../models/descriptors/FilterDescriptor";
import {Configuration} from "../../models/common/Configuration";

@Component({
    selector: "json-visualizer",
    template: `
        <mat-tab-group fxFlexFill="" mat-stretch-tabs dynamicHeight>
            <mat-tab label="Descriptor" fxFlexFill="" fxLayoutGap="15px">
                <div fxFlexFill="" fxLayout="column" fxLayoutGap="15px">
                    <button mat-icon-button fxFlex="10%" (click)="copyDesc()">
                        <mat-icon>event</mat-icon>
                    </button>
                    <ngx-json-viewer fxFlex="90%" [json]="descriptor"></ngx-json-viewer>
                </div>
            </mat-tab>
            <mat-tab label="Configuration">
                <div fxFlexFill="" fxLayout="column" fxLayoutGap="15px">
                    <button  mat-icon-button fxFlex="10%" (click)="copyConfig()">
                        <mat-icon>event</mat-icon>
                    </button>
                    <ngx-json-viewer fxFlex="90%" [json]="descriptor"></ngx-json-viewer>
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