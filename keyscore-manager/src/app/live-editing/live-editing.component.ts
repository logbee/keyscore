import {Component, OnInit} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {Observable} from "rxjs/index";
import {isSpinnerShowing} from "../common/loading/loading.reducer";
import {selectAppConfig} from "../app.config";
import {Configuration} from "../models/common/Configuration";
import {ResourceInstanceState} from "../models/filter-model/ResourceInstanceState";
import {
    selectInitialConfiguration,
    selectCurrentBlueprint,
    selectCurrentDescriptor,
    selectLiveEditingFilterState,
    selectUpdatedConfiguration
} from "./live-editing.reducer";
import "./live-editing-styles/live-editing.css";
import {Blueprint} from "../models/blueprints/Blueprint";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {RestoreFilterConfiguration, SaveUpdatedConfiguration, UpdateFilterConfiguration} from "./live-editing.actions";


@Component({
    selector: "live-editing",
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="filterName"
                (onManualRelad)="reload()">
        </header-bar>
        <div fxFlexFill="" fxLayout="row" fxLayoutGap="15" *ngIf="!(loading$ | async); else loading">
            <dataset-table class="live-editing-wrapper" fxFlex=""></dataset-table>
            <button *ngIf="!showConfigurator" matTooltip="Show Configuration." mat-mini-fab color="primary"
                    (click)="collapse()" class="collapseButton">
                <mat-icon>chevron_left</mat-icon>
            </button>
            <configurator *ngIf="showConfigurator" class="mat-elevation-z6" fxFlex="25"
                          [isOpened]=""
                          [collapsibleButton]="true"
                          [selectedBlock]="{configuration:(configuration$|async),
                                    descriptor:(descriptor$|async)}"
                          [showFooter]="true"
                          (onSave)="saveConfiguration($event)"
                          (onRevert)="revertFilterConfiguration()"
                          (onShowConfigurator)="showConfiguratorEvent($event)">
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
    public showConfigurator: boolean = true;


    private filterName: string = "Live-Editing";

    constructor(private store: Store<any>) {
        const config = this.store.select(selectAppConfig);
        config.subscribe((conf) => this.liveEditingFlag = conf.getBoolean("keyscore.manager.features.live-editing"));
        this.initialize();
    }

    public ngOnInit(): void {
        // this.error$.subscribe((cause) => this.triggerErrorComponent(cause.httpError));
        this.store.pipe(select(selectUpdatedConfiguration)).subscribe(config => {
                if (config) {
                    this.store.dispatch(new UpdateFilterConfiguration(config));
                }
            }
        );
    }


    collapse() {
        this.showConfigurator = true;
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
            this.configuration$ = this.store.pipe(select(selectInitialConfiguration));
        }
    }

    showConfiguratorEvent(show: boolean) {
        console.log("set showConfigurator to", show);
        this.showConfigurator = show;
    }

    saveConfiguration($event: Configuration) {
        this.store.dispatch(new SaveUpdatedConfiguration($event));
    }

    revertFilterConfiguration() {
        this.store.dispatch(new RestoreFilterConfiguration())
    }

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

