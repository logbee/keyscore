import {Component, EventEmitter, Input, Output} from "@angular/core";

@Component({
    selector: "left-right-control",
    template: `
        <div fxFlexFill="" style="cursor: pointer;" fxLayoutAlign="end" fxLayout="row" fxLayoutGap="15px">
            <div fxLayout="column" fxFlexFill>
                <div fxFlex="90">
                    <div fxLayout="row">
                        <mat-icon matTooltip="{{'NAVIGATION_CONTROL.MOVE_LEFT' | translate}}"
                                  (click)="navigateToLeft()">
                            chevron_left
                        </mat-icon>
                        <div matTooltip="{{'NAVIGATION_CONTROL.SWIPE_DATASETS' | translate}}">{{index + 1}} /
                            {{length}}
                        </div>
                        <mat-icon matTooltip="{{'NAVIGATION_CONTROL.MOVE_RIGHT' | translate}}"
                                  (click)="navigateToRight()">
                            chevron_right
                        </mat-icon>
                    </div>
                </div>
                <mat-label style="padding-left: 15px;font-size: small" fxFlex>{{label}}</mat-label>
            </div>
        </div>
    `
})


export class LeftToRightNavigationControl {
    @Input() public index: number;
    @Input() public length: number = 0;
    @Input() public label: string;
    @Output() public counterEvent: EventEmitter<number> = new EventEmitter();

    navigateToRight() {
        if (this.index == this.length - 1) {
            this.index = 0;
            this.emitCounter(this.index);
        } else {
            this.index += 1;
            this.emitCounter(this.index)
        }
    }

    navigateToLeft() {
        if (this.index == 0) {
            this.index = this.length - 1;
            this.emitCounter(this.index);
        } else {
            this.index -= 1;
            this.emitCounter(this.index)
        }
    }

    private emitCounter(index: number) {
        this.counterEvent.emit(index)
    }
}