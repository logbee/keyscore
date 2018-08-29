import {Component, OnDestroy, OnInit} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs/index";
import {isSpinnerShowing} from "../../common/loading/loading.reducer";
import {ErrorState, errorState} from "../../common/error/error.reducer";
import {selectAppConfig} from "../../app.config";
import {FilterConfiguration} from "../../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../../models/filter-model/FilterInstanceState";
import {
    selectExtractedDatasets,
    selectLiveEditingFilter,
    selectLiveEditingFilterState,
    selectResultDatasets
} from "../filter.reducer";
import {Dataset} from "../../models/filter-model/dataset/Dataset";
import {UpdateDatasetCounter, UpdateFilterConfiguration} from "../filters.actions";
import {Location} from "@angular/common";

@Component({
    selector: "live-editing",
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="filterName"
                (onManualRelad)="reload()">
        </header-bar>
        <div class="live-editing-wrapper">
            <div fxLayout="" fxLayoutGap="15px" *ngIf="!(loading$ | async); else loading">
                <div fxFlexFill="" fxLayoutGap="15px" fxLayout="column" fxLayout.xs="column" *ngIf="!errorHandling">
                    <filter-description fxFlex="20%" [currentFilter]="filter$ | async"
                                        [currentFilterState]="filterState$ | async">
                    </filter-description>
                    <example-message fxFlex="35%%" [extractedDatasets$]="extractedDatasets$"
                                     (currentDatasetCounter)="updateCounterInStore($event)">
                    </example-message>
                    <filter-configuration fxFlex="20%" [filter$]="filter$"
                                          [extractedDatasets$]="extractedDatasets$"
                                          (apply)="reconfigureFilter($event)"></filter-configuration>
                    <filter-result fxFlex="35%" [resultDatasets$]="resultDatasets$"
                                   (currentDatasetCounter)="updateCounterInStore($event)"></filter-result>
                </div>
            </div>
        </div>
        <error-component *ngIf="errorHandling" [httpError]="httpError"
                         [message]="message">
        </error-component>
        <ng-template #loading>
            <loading-full-view></loading-full-view>
        </ng-template>
    `
})

export class LiveEditingComponent implements OnInit, OnDestroy {
    // Flags
    private errorHandling: boolean = false;
    private liveEditingFlag: boolean;
    private httpError: string = "Ups!";
    private message: string = "The requested resource could not be shown";
    // Observables
    private filter$: Observable<FilterConfiguration>;
    private filterState$: Observable<FilterInstanceState>;
    private error$: Observable<ErrorState>;
    private loading$: Observable<boolean>;
    private extractedDatasets$: Observable<Dataset[]>;
    private resultDatasets$: Observable<Dataset[]>;
    private filterName: string = "Live-Editing";

    constructor(private store: Store<any>, private translate: TranslateService, private location: Location) {
        const config = this.store.select(selectAppConfig);
        config.subscribe((conf) => this.liveEditingFlag = conf.getBoolean("keyscore.manager.features.live-editing"));

        if (!this.liveEditingFlag) {
            this.triggerErrorComponent("999");
        } else {
            this.filterState$ = this.store.select(selectLiveEditingFilterState);
            this.filter$ = this.store.select(selectLiveEditingFilter);
            this.error$ = this.store.select(errorState);
            this.loading$ = this.store.select(isSpinnerShowing);
            this.extractedDatasets$ = this.store.select(selectExtractedDatasets);
            this.resultDatasets$ = this.store.select(selectResultDatasets);
        }
    }

    public ngOnInit(): void {
        this.error$.subscribe((cause) => this.triggerErrorComponent(cause.httpError));
    }

    public ngOnDestroy(): void {
    }

    public reconfigureFilter(update: { filterConfiguration: FilterConfiguration, values: any }) {
        this.store.dispatch(new UpdateFilterConfiguration(update.filterConfiguration, update.values));
    }

    private updateCounterInStore(count: number) {
        this.store.dispatch(new UpdateDatasetCounter(count));
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

