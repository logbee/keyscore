import {Component, EventEmitter, Input, OnDestroy, Output} from "@angular/core";
import {Subject, timer} from "rxjs";
import {takeUntil} from 'rxjs/operators';

@Component({
    selector: "confirm-button",
    template: `
        <button mat-button class="confirm-button"
                [style.background]="'linear-gradient(to left, ' + secondaryColor + ' 50%, ' + primaryColor + ' 50%)'"
                [style.background-position]="'right bottom'"
                [style.background-size]="'200% 100%'"
                [style.background-position-x]="-this.confirmation + '%'"
                (mousedown)="confirming($event)"
                (mouseup)="abort()"
                (mouseleave)="abort()">
            <ng-content></ng-content>
        </button>
    `,
    styleUrls: ['./confirm-button.component.scss']
})
export class ConfirmButtonComponent implements OnDestroy {

    @Output() confirmed: EventEmitter<void> = new EventEmitter();

    confirmation: number = 0;
    abort$ = new Subject<void>();

    primaryColor = "#f5f5f5";
    secondaryColor = "#AFAFAF";

    @Input() set kind(kind: string) {
        switch (kind) {
            case 'accept':
                this.secondaryColor = "#1dff5c";
                break;
            case 'caution':
                this.secondaryColor = "#ff546d";
                break;
            default:
                this.secondaryColor = "#AFAFAF";
        }
    }

    confirming(event: MouseEvent): void {
        if (event.ctrlKey) {
            this.confirm();
        }
        else {
            timer(50, 50)
                .pipe(takeUntil(this.abort$))
                .subscribe(tick => {
                    if (this.confirmation < 100) {
                        this.confirmation = 20 + tick * 10;
                    } else {
                        this.confirm()
                    }
                }, null, () => this.confirmation = 0);
        }
    }

    confirm(): void {
        this.abort$.next();
        this.confirmed.emit();
    }

    abort(): void {
        this.abort$.next();
    }

    ngOnDestroy(): void {
        this.abort();
        this.abort$.complete();
    }
}
