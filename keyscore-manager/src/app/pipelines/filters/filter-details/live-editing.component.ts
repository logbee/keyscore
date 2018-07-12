import {Component, OnInit} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs/index";
import {FilterConfiguration, FilterState, getLiveEditingFilter} from "../../pipelines.model";
import {isSpinnerShowing} from "../../../common/loading/loading.reducer";
import {ErrorState, errorState} from "../../../common/error/error.reducer";

@Component({
    selector: "live-editing",
    template: `
        <div *ngIf="!(loading$ | async); else loading">
            <div class="col-12" *ngIf="!errorHandling">
                <div class="card">
                    <div class="card-header alert-info">
                        {{'FILTERLIVEEDITINGCOMPONENT.TITLE' | translate}}
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
            <div class="col-12">
                <loading-full-view></loading-full-view>
            </div>
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

    constructor(private store: Store<FilterState>, private translate: TranslateService) {
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
        }
    }
}
