import {Component, EventEmitter, Input, Output} from "@angular/core";

@Component({
    selector: 'topToBottom-navigation-control',
    template: `
        <div fxFlexFill="" style="cursor: pointer;" fxLayout="column" fxLayoutGap="15px" fxLayoutAlign="end">
            <mat-icon fxFlex="" matTooltip="{{'NAVIGATION_CONTROL.MOVE_TOP' | translate}}" (click)="navigateToTop()" >expand_less</mat-icon>
            <div matTooltip="{{'NAVIGATION_CONTROL.SWIPE_RECORDS' | translate}}">{{index + 1}} / {{length}}</div>
            <mat-icon matTooltip="{{'NAVIGATION_CONTROL.MOVE_BOTTOM' | translate}}" (click)="navigateToBottom()">expand_more</mat-icon>
        </div>
    `
})

export class TopToBottomNavigationControlComponent {
    @Input() public index: number;
    @Input() public length: number;
    @Output() public counterEvent: EventEmitter<number> = new EventEmitter();


    navigateToTop() {
        if (this.index == this.length - 1) {
            this.index = 0;
            this.emitCounter(this.index);
        } else {
            this.index += 1;
            this.emitCounter(this.index)
        }
    }

    navigateToBottom() {
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