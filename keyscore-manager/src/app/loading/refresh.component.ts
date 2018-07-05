import {Component, EventEmitter, Input, Output} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: 'refresh-time',
    template: `
        <div class="dropdown">
            <button class="btn btn-outline-info dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown"
                    aria-haspopup="true" aria-expanded="false" (mouseenter)="setRefreshColor('white')" (mouseleave)="setRefreshColor('blue')">
                <img src="/assets/images/refresh-time-{{refreshColor}}.svg" height="24px" width="24px"> {{(refreshTime/1000)}}s
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
    @Output() update: EventEmitter<number> = new EventEmitter();
    private refreshColor:string;

    constructor(private translate: TranslateService) {
        this.refreshColor="blue"
    }

    updateRefreshTime(time: number) {
        this.update.emit(time);
    }

    setRefreshColor(color:string){
        this.refreshColor = color;
    }

}