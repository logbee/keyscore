import {Component, OnInit} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {Observable} from "rxjs/index";
import {isSpinnerShowing} from "../common/loading/loading.reducer";
import {selectAppConfig} from "../app.config";
import {Configuration} from "../models/common/Configuration";
import {ResourceInstanceState} from "../models/filter-model/ResourceInstanceState";
import {
    selectCurrentBlueprint,
    selectCurrentDataset,
    selectCurrentDescriptor,
    selectCurrentRecordIndex,
    selectInputDatasets,
    selectInitialConfiguration,
    selectLiveEditingFilterState,
    selectUpdatedConfiguration
} from "./live-editing.reducer";
import "./live-editing-styles/live-editing.css";
import {Blueprint} from "../models/blueprints/Blueprint";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {
    LoadAllPipelinesForRedirect,
    RestoreFilterConfiguration,
    SaveUpdatedConfiguration,
    UpdateConfigurationInBackend,
    UpdateFilterConfiguration
} from "./live-editing.actions";
import {Dataset} from "../models/dataset/Dataset";
import {DatasetTableModel} from "../models/dataset/DatasetTableModel";


@Component({
    selector: "live-editing",
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="filterName"
                (onManualRelad)="reload()">
        </header-bar>
        <div fxLayout="row" style="height:95vh;" fxLayoutGap="15" *ngIf="!(loading$ | async); else loading">
            <div fxLayout="column" fxLayoutGap="15px" fxFlex="">
                <div fxFlex="" fxLayout="row" fxLayoutGap="15px">
                    <button mat-icon-button>
                        <mat-icon svgIcon="navigate-to-pipely" matTooltip="Navigate to Pipely." matTooltipPosition="after" (click)="navigateToPipely()">
                        </mat-icon>
                    </button>

                    <div fxFlex="90"></div>
                    <button fxFlex="5" fxLayoutAlign="end" *ngIf="!showConfigurator" matTooltip="{{'CONFIGURATOR.SHOW' | translate}}" mat-mini-fab
                            color="primary"
                            (click)="show()" class="collapseButton">
                        <mat-icon>chevron_left</mat-icon>
                    </button>
                </div>
                <div *ngIf="(datasets$ | async).length !== 0; else disclaimer" fxFlex="95" fxFlexFill="" fxLayout="row" fxLayoutGap="15px">
                    <dataset-table  fxFlex="" class="live-editing-wrapper"></dataset-table>
                </div>
            </div>
            <configurator *ngIf="showConfigurator" class="mat-elevation-z6" fxFlex="25"
                          [collapsibleButton]="true"
                          [selectedBlock]="{configuration:(configuration$|async),
                                    descriptor:(filterDescriptor$|async)}"
                          [showFooter]="true" 
                          [datasets]="datasets$ | async"
                          (onSave)="saveConfiguration($event)"
                          (onRevert)="revertFilterConfiguration()"
                          (onShowConfigurator)="hide($event)"
                          (onOverwriteConfiguration)="overwriteConfiguration()">
            </configurator>
        </div>
        <error-component *ngIf="errorHandling" [httpError]="httpError"
                         [message]="message">
        </error-component>
        
        <ng-template #loading>
            <loading-full-view></loading-full-view>
        </ng-template>

        <ng-template #disclaimer>
            <mat-label fxFlex="" fxLayoutAlign="center">{{'FILTERLIVEEDITINGCOMPONENT.NODATA' | translate}}</mat-label>
        </ng-template>
    `
})

export class LiveEditingComponent implements OnInit {
    //Observe a balls
    private loading$: Observable<boolean>;
    private blueprint$: Observable<Blueprint>;
    private configuration$: Observable<Configuration>;
    private filterState$: Observable<ResourceInstanceState>;
    private filterDescriptor$: Observable<ResolvedFilterDescriptor>;
    private datasets$: Observable<Dataset[]>;

    //Flags
    private currentConfiguration: Configuration;
    private showConfigurator: boolean = true;
    private liveEditingEnabled: boolean;
    private filterName: string = "Live-Editing";


    constructor(private store: Store<any>) {
       //check if live-editing feature toggle is on.
        const config = this.store.select(selectAppConfig);
        config.subscribe((conf) => this.liveEditingEnabled = conf.getBoolean("keyscore.manager.features.live-editing"));
        //initalize alle observables from store that are needed.
        if(this.liveEditingEnabled) {
            this.initialize();
        } else {
            //TODO: show error component
        }
    }

    public ngOnInit(): void {
        //subscribe on updatedConfiguration in state to trigger the reconfiguring of the Filter
        this.store.pipe(select(selectUpdatedConfiguration)).subscribe(config => {
                if (config) {
                    this.currentConfiguration = config;
                    this.store.dispatch(new UpdateFilterConfiguration(config));
                }
            }
        );
    }

    navigateToPipely() {
        this.store.dispatch(new LoadAllPipelinesForRedirect());
    }

    private initialize() {
            this.datasets$ = this.store.pipe(select(selectInputDatasets));
            this.loading$ = this.store.pipe(select(isSpinnerShowing));
            this.filterState$ = this.store.pipe(select(selectLiveEditingFilterState));
            this.filterDescriptor$ = this.store.pipe(select(selectCurrentDescriptor));
            this.blueprint$ = this.store.pipe(select(selectCurrentBlueprint));
            this.configuration$ = this.store.pipe(select(selectInitialConfiguration));
    }

    saveConfiguration($event: Configuration) {
        this.store.dispatch(new SaveUpdatedConfiguration($event));
    }

    overwriteConfiguration() {
        this.store.dispatch(new UpdateConfigurationInBackend(this.currentConfiguration))
    }

    revertFilterConfiguration() {
        this.store.dispatch(new RestoreFilterConfiguration())
    }

    // Configurator collapse methods

    hide() {
        this.showConfigurator = false;
    }

    show() {
        this.showConfigurator = true;
    }
}