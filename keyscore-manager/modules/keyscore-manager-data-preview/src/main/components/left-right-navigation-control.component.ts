import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";

@Component({
    selector: "left-right-control",
    template: `
        <div fxFlexFill="" style="cursor: pointer;" fxLayoutAlign="end" fxLayout="row" fxLayoutGap="15px">
            <div fxLayout="column" fxFlexFill fxLayoutAlign="start center">
                <mat-label style="font-size: small" fxFlex>{{label}}</mat-label>
                <div>
                    <div fxLayout="row" fxLayoutAlign="start center">
                        <button mat-icon-button aria-label="navigate to previous entry"
                                matTooltip="{{'NAVIGATION_CONTROL.MOVE_LEFT' | translate}}"
                                (click)="navigateToLeft()"
                                [disabled]="length < 2"
                        >
                            <mat-icon>chevron_left</mat-icon>
                        </button>
                        <div matTooltip="{{'NAVIGATION_CONTROL.SWIPE_DATASETS' | translate}}">{{index}} /
                            {{length}}
                        </div>
                        <button mat-icon-button aria-label="navigate to next entry"
                                matTooltip="{{'NAVIGATION_CONTROL.MOVE_RIGHT' | translate}}"
                                (click)="navigateToRight()"
                                [disabled]="length < 2"
                        >
                            <mat-icon>
                                chevron_right
                            </mat-icon>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `
})


export class LeftToRightNavigationControl implements OnInit {

    @Input() index: number = 0;

    @Input() set length(val: number) {
        this._length = val;
        if (this.length === 0) {
            this.index = 0;
        } else if (this.length > 0 && this.index === 0) {
            this.index = 1;
        }
    }

    get length(): number {
        return this._length;
    }

    private _length: number = 0;


    @Input() label: string;
    @Output() counterEvent: EventEmitter<number> = new EventEmitter();

    ngOnInit(): void {
        if (this.length === 0) {
            this.index = 0;
        }
    }

    navigateToRight() {
        if (this.length === 0) return;

        if (this.index == this.length) {
            this.index = 1;
        } else {
            this.index += 1;
        }

        this.emitCounter(this.index)
    }

    navigateToLeft() {
        if (this.length === 0) return;

        if (this.index == 1) {
            this.index = this.length;
        } else {
            this.index -= 1;
        }

        this.emitCounter(this.index)
    }

    private emitCounter(index: number) {
        this.counterEvent.emit(index)
    }
}
