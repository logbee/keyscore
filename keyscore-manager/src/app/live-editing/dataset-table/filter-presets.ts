import {Component, EventEmitter, Output} from "@angular/core";

@Component({
    selector: "filter-presets",
    template: `
        <div fxLayout="row" fxFlexFill="" fxLayoutGap="30px">
            <button matTooltip="{{'FILTERLIVEEDITINGCOMPONENT.PRESET_IN' | translate}}" fxFlex="3"
                    mat-icon-button (click)="changeViewPreset('showOnlyInput')">
                <mat-icon>border_left</mat-icon>
            </button>

            <button matTooltip="{{'FILTERLIVEEDITINGCOMPONENT.PRESET_ALL' | translate}}" fxFlex="3"
                    mat-icon-button (click)="changeViewPreset('showEverything')">
                <mat-icon>border_vertical</mat-icon>
            </button>

            <button matTooltip="{{'FILTERLIVEEDITINGCOMPONENT.PRESET_OUT' | translate}}" fxFlex="3"
                    mat-icon-button (click)="changeViewPreset('showOnlyOutput')">
                <mat-icon>border_right</mat-icon>
            </button>
        </div>
    `
})

export class FilterPresets {
    @Output() public preset: EventEmitter<string> = new EventEmitter();


    changeViewPreset(value: string) {
        switch (value) {
            case "showOnlyInput":
                this.preset.emit("showOnlyInput");
                break;
            case "showEverything":
                this.preset.emit("showEverything");
                break;
            case "showOnlyOutput":
                this.preset.emit("showOnlyOutput");
                break;
        }
    }
}