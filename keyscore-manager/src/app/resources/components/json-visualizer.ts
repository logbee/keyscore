import {Component} from "@angular/core";
import {ResolvedFilterDescriptor} from "../../models/descriptors/FilterDescriptor";
import {Store} from "@ngrx/store";
import {selectConfiguration, selectDescriptor} from "../resources.reducer";
import {Observable} from "rxjs/index";
import {Configuration} from "../../models/common/Configuration";

@Component({
    selector: "json-visualizer",
    template: `
        <mat-tab-group fxFlexFill="" mat-stretch-tabs dynamicHeight>
            <mat-tab label="Descriptor" fxFlexFill="" fxLayoutGap="15px">
                <div fxFlexFill="" fxLayoutGap="15px">
                    <ngx-json-viewer fxFlex="90%" [json]="descriptor$ | async"></ngx-json-viewer>
                    <button matTooltipPosition="after" matTooltip="Copy Json" mat-icon-button fxFlex="10%"
                            (click)="copy('descriptor')">
                        <mat-icon>content_copy</mat-icon>
                    </button>
                </div>
            </mat-tab>
            <mat-tab label="Configuration">
                <div fxFlexFill="" fxLayoutGap="15px">
                    <ngx-json-viewer fxFlex="90%" [json]="configuration$ | async"></ngx-json-viewer>
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
    private descriptor$: Observable<ResolvedFilterDescriptor>;
    private configuration$: Observable<Configuration>;

    constructor(private store: Store<any>) {
        this.configuration$ = this.store.select(selectConfiguration);
        this.descriptor$ = this.store.select(selectDescriptor);
    }

    copy(which: string) {
        let copiedElement:any;
        if (which == "descriptor") {
            this.descriptor$.subscribe(desc => copiedElement = desc);
        } else if(which == "configuration")  {
            this.configuration$.subscribe(conf => copiedElement = conf);
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