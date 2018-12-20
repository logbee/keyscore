import {Component, EventEmitter, Input, Output} from "@angular/core";

@Component({
    selector: "leftTotRight-navigation-control",
    template: `
        <div fxFlexFill="" style="cursor: pointer;" fxLayoutAlign="end" fxLayout="row" fxLayoutGap="15px">
            <mat-icon matTooltip="{{'NAVIGATION_CONTROL.MOVE_LEFT' | translate}}" (click)="navigateToLeft()">
                chevron_left
            </mat-icon>
            <div matTooltip="{{'NAVIGATION_CONTROL.SWIPE_DATASETS' | translate}}">{{index + 1}} / {{length}}</div>
            <mat-icon matTooltip="{{'NAVIGATION_CONTROL.MOVE_RIGHT' | translate}}" (click)="navigateToRight()">
                chevron_right
            </mat-icon>
        </div>
    `
})


export class LeftToRightNavigationControl {
    @Input() public index: number;
    @Input() public length: number;
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