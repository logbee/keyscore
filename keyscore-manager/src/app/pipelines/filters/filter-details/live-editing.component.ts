import {Component, OnInit} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs/index";
import {FilterConfiguration, getLiveEditingFilter} from "../../pipelines.model";
import {isSpinnerShowing} from "../../../common/loading/loading.reducer";
import {ErrorState, errorState} from "../../../common/error/error.reducer";
import {b} from "@angular/core/src/render3";
import {AppState} from "../../../app.component";
import {selectAppConfig} from "../../../app.config";

@Component({
    selector: "live-editing",
    template: `
        <div *ngIf="!(loading$ | async); else loading">
            <div class="col-12" *ngIf="!errorHandling">
                <div class="card mt-3 mb-3">
                    <div class="card-header" style="background-color: #365880; color: white">
                        <strong>{{'FILTERLIVEEDITINGCOMPONENT.TITLE' | translate}}</strong>
                    </div>
                    <div class="card-body badge-light">

                        <filter-description [currentFilter]="filter$ | async"></filter-description>

                        <example-message></example-message>

                        <pattern (apply)="applyConfiguration($event)"></pattern>

                        <filter-result></filter-result>

                        <button class="mt-3 btn float-right primary btn-success"> {{'GENERAL.SAVE' | translate}}
                        </button>
                    </div>
                </div>
            </div>
            <div class="col-12">
                <error-component *ngIf="errorHandling" [httpError]="httpError"
                                 [message]="message"></error-component>
            </div>
        </div>
        <ng-template #loading>
                <loading-full-view></loading-full-view>
        </ng-template>
    `
})

export class LiveEditingComponent implements OnInit {
    private httpError: string = "Ups!";
    private message: string = "Keyscore.exe hast stopped working";
    private filter$: Observable<FilterConfiguration>;
    private errorHandling: boolean = false;
    private error$: Observable<ErrorState>;
    private loading$: Observable<boolean>;
    private liveEditingFlag: boolean;
    constructor(private store: Store<AppState>, private translate: TranslateService) {
        const config = this.store.select(selectAppConfig);
        config.subscribe((conf) => this.liveEditingFlag = conf.getBoolean("keyscore.manager.features.live-editing"));
        if (!this.liveEditingFlag) {
            this.triggerErrorComponent("999");
        }
        this.loading$ = this.store.select(isSpinnerShowing);
        this.error$ = this.store.select(errorState);
        this.filter$ = this.store.select(getLiveEditingFilter);
    }

    public ngOnInit() {
        this.error$.subscribe((cause) => this.triggerErrorComponent(cause.httpError));
    }

    public applyConfiguration(regex: string) {
        console.log("applyConfiguration:" + regex);
    }

    private triggerErrorComponent(httpError: string) {
        console.log(httpError);
        switch (httpError.toString()) {
            case "404": {
                this.httpError = httpError;
                this.translate.get("ERRORS.404").subscribe(
                    (translation) => this.message = translation);
                this.errorHandling = true;
                break;
            }
            case "503": {
                this.httpError = httpError;
                this.translate.get("ERRORS.503").subscribe(
                    (translation) => this.message = translation);
                this.errorHandling = true;
                break;
            }
            case "0": {
                this.translate.get("ERRORS.0").subscribe(
                    (translation) => this.message = translation);
                this.errorHandling = true;
                break;
            }
            case "999": {
                this.translate.get("ERRORS.999").subscribe(
                    (translation) => this.message = translation);
                this.errorHandling = true;
                break;
            }
        }
    }
}
