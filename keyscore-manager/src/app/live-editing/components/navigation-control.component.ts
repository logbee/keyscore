import {Component, EventEmitter, Input, Output} from "@angular/core";

@Component({
    selector: "navigation-control",
    template: `
        <div fxFlexFill="" fxLayout="row" fxLayoutGap="15px">
                <mat-icon (click)="navigateToLeft()">chevron_left</mat-icon>
                    <div>{{index + 1}} / {{length}}</div>
                <mat-icon (click)="navigateToRight()">chevron_right</mat-icon>
        </div>
    `
})


export class NavigationControlComponent {
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