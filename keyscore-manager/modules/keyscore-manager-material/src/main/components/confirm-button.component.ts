import {Component, EventEmitter, OnDestroy, Output} from "@angular/core";
import {Subject, timer} from "rxjs";
import {takeUntil} from 'rxjs/operators';

@Component({
    selector: "confirm-button",
    template: `
        <button mat-button class="confirm-button" [style.background-position-x]="-this.confirmation + '%'" (mousedown)="confirming()" (mouseup)="abort()" (mouseleave)="abort()">Confirm</button>
    `,
    styleUrls: ['./confirm-button.component.scss']
})
export class ConfirmButtonComponent implements OnDestroy {

    @Output() confirmed: EventEmitter<void> = new EventEmitter();

    private confirmation: number = 0;
    private abort$ = new Subject<void>();

    private confirming(): void {
        timer(100,100)
            .pipe(takeUntil(this.abort$))
            .subscribe(tick => {
                console.log("Tick: ", tick);
                if (this.confirmation < 100) {
                    this.confirmation = tick * 10;
                }
                else {
                    this.confirm()
                }
            }, null, () => this.confirmation = 0);
    }

    private confirm(): void {
        console.log("Confirmed");
        this.abort$.next();
        this.confirmed.emit();
    }

    private abort(): void {
        this.abort$.next();
    }

    ngOnDestroy(): void {
        this.abort();
        this.abort$.complete();
    }
}
