import {Component, OnInit} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs/index";
import {isSpinnerShowing} from "../common/loading/loading.reducer";
import {selectAppConfig} from "../app.config";
import {Configuration} from "../models/common/Configuration";
import {ResourceInstanceState} from "../models/filter-model/ResourceInstanceState";
import {
    selectConfiguration,
    selectCurrentBlueprint,
    selectCurrentDescriptor,
    selectExtractedDatasets,
    selectLiveEditingFilterState, selectUpdatedConfiguration
} from "./live-editing.reducer";
import "./live-editing-styles/live-editing.css";
import {Dataset} from "../models/dataset/Dataset";
import {Blueprint} from "../models/blueprints/Blueprint";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {
    LoadFilterStateAction,
    LoadFilterStateSuccess,
    SaveUpdatedConfiguration,
    UpdateFilterConfiguration
} from "./live-editing.actions";


@Component({
    selector: "live-editing",
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="filterName"
                (onManualRelad)="reload()">
        </header-bar>
        <div fxFlexFill="" fxLayout="row" fxLayoutGap="15px" *ngIf="!(loading$ | async); else loading">
            <dataset-table class="live-editing-wrapper" fxFlex="75%"></dataset-table>
            <configurator class="mat-elevation-z6" fxFlex=""
                          [isOpened]=""
                          [selectedBlock]="{configuration:(configuration$|async),
                                    descriptor:(descriptor$|async)}"
                          [showFooter]="true"
                          (onSave)="saveConfiguration($event)"
                          (closeConfigurator)="closeConfigurator()">
            </configurator>

        </div>
        <error-component *ngIf="errorHandling" [httpError]="httpError"
                         [message]="message">
        </error-component>
        <ng-template #loading>
            <loading-full-view></loading-full-view>
        </ng-template>
    `
})

export class LiveEditingComponent implements OnInit {
    // Flags

    // private errorHandling: boolean = false;
    // private error$: Observable<ErrorState>;
    private loading$: Observable<boolean>;
    private liveEditingFlag: boolean;
    private blueprint$: Observable<Blueprint>;
    private message: string = "The requested resource could not be shown";
    // // Observables
    private configuration$: Observable<Configuration>;
    private filterState$: Observable<ResourceInstanceState>;
    private descriptor$: Observable<ResolvedFilterDescriptor>;


    private extractedDatasets$: Observable<Dataset[]>;
    // private resultDatasets$: Observable<Dataset[]>;
    private filterName: string = "Live-Editing";

    constructor(private store: Store<any>, private translate: TranslateService) {
        const config = this.store.select(selectAppConfig);
        config.subscribe((conf) => this.liveEditingFlag = conf.getBoolean("keyscore.manager.features.live-editing"));
        this.initialize();
    }

    public ngOnInit(): void {
        // this.error$.subscribe((cause) => this.triggerErrorComponent(cause.httpError));
        this.store.pipe(select(selectUpdatedConfiguration)).subscribe(config => {
                console.log("Triggered config subscription config is" + JSON.stringify(config));
                if (config) {
                    this.store.dispatch(new UpdateFilterConfiguration(config));
                }
            }
        );
    }

    private initialize() {
        if (!this.liveEditingFlag) {
            // this.triggerErrorComponent("999");
        }
        else {
            // this.error$ = this.store.pipe(select(errorState));
            this.loading$ = this.store.pipe(select(isSpinnerShowing));
            this.filterState$ = this.store.pipe(select(selectLiveEditingFilterState));
            this.descriptor$ = this.store.pipe(select(selectCurrentDescriptor));
            this.blueprint$ = this.store.pipe(select(selectCurrentBlueprint));
            this.configuration$ = this.store.pipe(select(selectConfiguration));
            this.extractedDatasets$ = this.store.pipe(select(selectExtractedDatasets));
            // this.resultDatasets$ = this.store.select(selectResultDatasets);
        }
    }

    saveConfiguration($event: Configuration) {
        this.store.dispatch(new SaveUpdatedConfiguration($event));
    }

    closeConfigurator() {

    }

    // private updateCounterInStore(count: number) {
    //     this.store.dispatch(new UpdateDatasetCounter(count));
    // }

    // private triggerErrorComponent(httpError: string) {
    //     switch (httpError.toString()) {
    //         case "404": {
    //             this.translate.get("ERRORS.404").subscribe(
    //                 (translation) => this.message = translation);
    //             this.errorHandling = true;
    //             break;
    //         }
    //         case "503": {
    //             this.translate.get("ERRORS.503").subscribe(
    //                 (translation) => this.message = translation);
    //             this.errorHandling = true;
    //             break;
    //         }
    //         case "0": {
    //             this.translate.get("ERRORS.0").subscribe(
    //                 (translation) => this.message = translation);
    //             this.errorHandling = true;
    //             break;
    //         }
    //         case "999": {
    //             this.translate.get("ERRORS.999").subscribe(
    //                 (translation) => this.message = translation);
    //             this.errorHandling = true;
    //             break;
    //         }
    //     }
    // }

}

