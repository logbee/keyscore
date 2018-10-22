import {Component, EventEmitter, Input, Output} from "@angular/core";

@Component({
    selector: "navigation-control",
    template: `
        <div fxFlexFill="" fxLayout="row" fxLayoutGap="15px">
            <mat-icon (click)="navigateDatasetsToLeft()">chevron_left</mat-icon>
            <div>{{index + 1}} / {{length}}</div>
            <mat-icon (click)="navigateDatasetsToRight()">chevron_right</mat-icon>
        </div>
    `
})


export class NavigationControlComponent {
    @Input() public index: number;
    @Input() public length: number;
    @Output() public counterEvent: EventEmitter<number> = new EventEmitter();

    navigateDatasetsToRight() {
        if (this.index == this.length - 1) {
            this.index = 0;
            this.emitCounter(this.index);
        } else {
            this.index += 1;
            this.emitCounter(this.index)
        }
    }

    navigateDatasetsToLeft() {
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