import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {Observable} from "rxjs/internal/Observable";
import {delay, filter, takeWhile, tap} from "rxjs/operators";
import "./alert.style.css";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: "alert",
    template: `
        <div class="alertComponent  {{active}}">
            <div class="alert alert-{{level}}" role="alert">{{message | translate}}</div>
        </div>
    `
})

export class AlertComponent implements OnDestroy, OnInit {
    /**
     * The message which should be shown in the alert.
     * Put the key from the language file here!!
     */
    @Input() public message: string;
    /**
     * Observable which triggers the alert dialogue on emitting true
     */
    @Input() public trigger$: Observable<boolean>;
    /**
     * the level of the alert. This effects the color.
     * Possible values are: primary,secondary,success,danger,warning,info,light,dark
     * The default value is primary.
     */
    @Input() public level: string;
    /**
     * the time in milliseconds until the alert disappears. The default value is 5000.
     */
    @Input() public timeout: number = 5000;

    private isAlive: boolean = true;
    private validLevelValues: string[] =
        ["primary", "secondary", "success", "danger", "warning", "info", "light", "dark"];
    private active: string = "";

    constructor(private translate: TranslateService) {

    }

    public ngOnInit() {
        this.level = this.validLevelValues.includes(this.level) ? this.level : this.validLevelValues[0];
        this.trigger$.pipe(takeWhile((_) => this.isAlive),
            filter((trigger) => trigger === true),
            tap((_) => this.active = "active"),
            delay(this.timeout)
        ).subscribe((_) => this.active = "");
    }

    public ngOnDestroy() {
        this.isAlive = false;
    }
}
