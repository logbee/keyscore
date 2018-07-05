import {Component, EventEmitter, Input, Output} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: 'refresh-time',
    template: `
        <div class="dropdown">
            <button class="btn btn-info dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown"
                    aria-haspopup="true" aria-expanded="false" >
                <img src="/assets/images/refresh-time-white.svg" height="24px" width="24px"> {{(refreshTime>0 ? (refreshTime/1000)+'s': ('REFRESHCOMPONENT.OF') | translate)}}
            </button>
            <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                <a class="dropdown-item" (click)="updateRefreshTime(-1)">{{'REFRESHCOMPONENT.OF' | translate}}</a>
                <a class="dropdown-item" (click)="updateRefreshTime(5000)">5s</a>
                <a class="dropdown-item" (click)="updateRefreshTime(10000)">10s</a>
                <a class="dropdown-item" (click)="updateRefreshTime(15000)">15s</a>
                <a class="dropdown-item" (click)="updateRefreshTime(30000)">30s</a>
            </div>
        </div>
    `
})

export class RefreshTimeComponent {
    @Input() refreshTime: number;
    @Output() update: EventEmitter<{newRefreshTime:number,oldRefreshTime:number}> = new EventEmitter();

    constructor(private translate: TranslateService) {
    }

    updateRefreshTime(time: number) {
        this.update.emit({newRefreshTime:time,oldRefreshTime:this.refreshTime});
    }


}