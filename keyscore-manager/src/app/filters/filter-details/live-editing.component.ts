import {Component, OnInit} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs/index";
import {isSpinnerShowing} from "../../common/loading/loading.reducer";
import {ErrorState, errorState} from "../../common/error/error.reducer";
import {selectAppConfig} from "../../app.config";
import {FilterConfiguration} from "../../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../../models/filter-model/FilterInstanceState";
import {
    getExtractedDatasetsByIndex,
    getLiveEditingFilter,
    getLiveEditingFilterState
} from "../filter.reducer";
import {Dataset} from "../../models/filter-model/dataset/Dataset";

@Component({
    selector: "live-editing",
    template: `
        <header-bar
                title="Live-Editing"
                [showManualReload]="false"
                (onManualReload)="reload()">
        </header-bar>
        <div *ngIf="!(loading$ | async); else loading">
            <div class="col-12" *ngIf="!errorHandling">
                <div class="card-body badge-light">
                    <filter-description [currentFilter]="filter$ | async"
                                        [currentFilterState]="filterState$ | async">
                    </filter-description>
                    <example-message [dataset]="dataset$ | async"></example-message>
                    <pattern></pattern>
                    <filter-result></filter-result>
                    <button class="mt-3 btn float-right primary btn-success"> {{'GENERAL.SAVE' | translate}}
                    </button>
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
    // Flags
    private errorHandling: boolean = false;
    private liveEditingFlag: boolean;
    private httpError: string = "Ups!";
    private message: string = "The requested resource could not be shown";
    // Obsevables
    private filter$: Observable<FilterConfiguration>;
    private filterState$: Observable<FilterInstanceState>;
    private error$: Observable<ErrorState>;
    private loading$: Observable<boolean>;
    private dataset$: Observable<Dataset>;

    constructor(private store: Store<any>, private translate: TranslateService) {
        const config = this.store.select(selectAppConfig);
        config.subscribe((conf) => this.liveEditingFlag = conf.getBoolean("keyscore.manager.features.live-editing"));
        if (!this.liveEditingFlag) {
            this.triggerErrorComponent("999");
        } else {
            this.filterState$ = this.store.select(getLiveEditingFilterState);
            this.filter$ = this.store.select(getLiveEditingFilter);
            this.error$ = this.store.select(errorState);
            this.loading$ = this.store.select(isSpinnerShowing);
            this.dataset$ = this.store.select(getExtractedDatasetsByIndex);
        }
    }

    public ngOnInit(): void {
        this.error$.subscribe((cause) => this.triggerErrorComponent(cause.httpError));
    }

    private triggerErrorComponent(httpError: string) {
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
