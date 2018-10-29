import {Component, OnInit} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {BehaviorSubject, Observable} from "rxjs/index";
import {isSpinnerShowing} from "../common/loading/loading.reducer";
import {selectAppConfig} from "../app.config";
import {Configuration} from "../models/common/Configuration";
import {ResourceInstanceState} from "../models/filter-model/ResourceInstanceState";
import {
    selectCurrentBlueprint,
    selectCurrentDescriptor,
    selectInitialConfiguration,
    selectLiveEditingFilterState,
    selectUpdatedConfiguration
} from "./live-editing.reducer";
import "./live-editing-styles/live-editing.css";
import {Blueprint} from "../models/blueprints/Blueprint";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {RestoreFilterConfiguration, SaveUpdatedConfiguration, UpdateFilterConfiguration} from "./live-editing.actions";
import {BlockDescriptor} from "../pipelines/pipeline-editor/pipely/models/block-descriptor.model";


@Component({
    selector: "live-editing",
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="filterName"
                (onManualRelad)="reload()">
        </header-bar>
        <div fxLayout="row" style="height: calc(95vh);" fxLayoutGap="15" *ngIf="!(loading$ | async); else loading">
            <dataset-table class="live-editing-wrapper" fxFlex=""></dataset-table>
            <button *ngIf="!showConfigurator" matTooltip="{{'CONFIGURATOR.SHOW' | translate}}" mat-mini-fab color="primary"
                    (click)="show()" class="collapseButton">
                <mat-icon>chevron_left</mat-icon>
            </button>
            <configurator *ngIf="showConfigurator" class="mat-elevation-z6" fxFlex="25"
                          [collapsibleButton]="true"
                          [selectedBlock]="{configuration:(configuration$|async),
                                    descriptor:(descriptor$|async)}"
                          [showFooter]="true"
                          (onSave)="saveConfiguration($event)"
                          (onRevert)="revertFilterConfiguration()"
                          (onShowConfigurator)="hide($event)">
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
    private showConfigurator: boolean = true;



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


    show() {
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

    hide() {
        this.showConfigurator = false;
    }

    saveConfiguration($event: Configuration) {
        this.store.dispatch(new SaveUpdatedConfiguration($event));
    }

    revertFilterConfiguration() {
        this.store.dispatch(new RestoreFilterConfiguration())
    }

}