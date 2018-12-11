import {Component, EventEmitter, Input, Output} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: "refresh-time",
    template: `
        <div class="dropdown">
            <button mat-stroked-button class="mat-white-stroked-button" [matMenuTriggerFor]="refreshMenu">
                <mat-icon>timer</mat-icon>
                {{(refreshTime > 0 ? (refreshTime / 1000) + 's' : ('REFRESHCOMPONENT.OF') | translate)}}
            </button>
            <mat-menu #refreshMenu>
                <button mat-menu-item (click)="updateRefreshTime(-1)">{{'REFRESHCOMPONENT.OF' | translate}}</button>
                <button mat-menu-item (click)="updateRefreshTime(5000)">5s</button>
                <button mat-menu-item (click)="updateRefreshTime(10000)">10s</button>
                <button mat-menu-item (click)="updateRefreshTime(15000)">15s</button>
                <button mat-menu-item (click)="updateRefreshTime(30000)">30s</button>
            </mat-menu>
        </div>
    `
})

export class RefreshTimeComponent {
    @Input() public refreshTime: number;
    @Output() public update: EventEmitter<{ newRefreshTime: number, oldRefreshTime: number }> = new EventEmitter();

    constructor(private translate: TranslateService) {
    }

    public updateRefreshTime(time: number) {
        this.update.emit({newRefreshTime: time, oldRefreshTime: this.refreshTime});
    }

}
