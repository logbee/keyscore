import {Injectable} from "@angular/core";
import {AppState} from "../app.component";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {forkJoin, Observable, of} from "rxjs/index";
import {ROUTER_NAVIGATION, RouterNavigationAction} from "@ngrx/router-store";
import {catchError, concatMap, map, mergeMap, withLatestFrom} from "rxjs/internal/operators";
import {Action, select, Store} from "@ngrx/store";
import {
    DRAIN_FILTER,
    DRAIN_FILTER_FAILURE,
    DrainFilterAction,
    DrainFilterFailure,
    DrainFilterSuccess,
    EXTRACT_DATASETS,
    EXTRACT_DATASETS_FAILURE,
    ExtractDatasetsAction,
    ExtractDatasetsFailure,
    ExtractDatasetsResultSuccess,
    InitialExtractSuccess,
    INSERT_DATASETS_FAILURE,
    INSERT_DATASETS_SUCCESS,
    InsertDatasetsFailure,
    InsertDatasetsSuccess,
    LOAD_ALL_PIPELINES_FOR_REDIRECT,
    LOAD_DESCRIPTOR_FOR_BLUEPRINT,
    LOAD_DESCRIPTOR_FOR_BLUEPRINT_SUCCESS,
    LOAD_FILTER_BLUEPRINT_FAILURE,
    LOAD_FILTER_BLUEPRINT_SUCCESS,
    LOAD_FILTER_CONFIGURATION,
    LOAD_FILTER_CONFIGURATION_FAILURE,
    LOAD_FILTERSTATE,
    LOAD_FILTERSTATE_FAILURE,
    LoadDescriptorForBlueprint,
    LoadDescriptorForBlueprintSuccess,
    LoadFilterBlueprintFailure,
    LoadFilterBlueprintSuccess,
    LoadFilterConfigurationAction,
    LoadFilterConfigurationFailure,
    LoadFilterConfigurationSuccess,
    LoadFilterStateAction,
    LoadFilterStateFailure,
    LoadFilterStateSuccess,
    NAVIAGATE_TO_PIPELY_FAILURE,
    NaviagatetoPipelyFailure, OverwriteSuccess,
    PAUSE_FILTER,
    PAUSE_FILTER_FAILURE,
    PauseFilterAction,
    PauseFilterFailure,
    PauseFilterSuccess,
    RECONFIGURE_FILTER_FAILURE,
    RECONFIGURE_FILTER_SUCCESS,
    ReconfigureFilterFailure,
    ReconfigureFilterSuccess,
    ResetAction,
    ResolvedDescriptorForBlueprintSuccess,
    RESTORE_FILTER_CONFIGURATION,
    RestoreFilterConfiguration, UPDATE_CONFIGURATION_IN_BACKEND,
    UPDATE_FILTER_CONFIGURATION, UpdateConfigurationInBackend,
    UpdateFilterConfiguration
} from "./live-editing.actions";
import {BlueprintService} from "../services/rest-api/BlueprintService";
import {Configuration} from "../models/common/Configuration";
import {Blueprint, PipelineBlueprint} from "../models/blueprints/Blueprint";
import {ResourceInstanceState} from "../models/filter-model/ResourceInstanceState";
import {Descriptor} from "../models/descriptors/Descriptor";
import {switchMap} from "rxjs/operators";
import {
    LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_FAILURE,
    LoadAllDescriptorsForBlueprintFailureAction
} from "../resources/resources.actions";
import {DescriptorResolverService} from "../services/descriptor-resolver.service";
import {selectCurrentBlueprint, selectInputDatasets, selectInitialConfiguration} from "./live-editing.reducer";
import {Dataset} from "../models/dataset/Dataset";
import {FilterControllerService} from "../services/rest-api/FilterController.service";
import {ConfigurationService} from "../services/rest-api/ConfigurationService";
import {DescriptorService} from "../services/rest-api/DescriptorService";
import {SnackbarOpen} from "../common/snackbar/snackbar.actions";
import {PipelineService} from "../services/rest-api/PipelineService";
import * as RouterActions from "../router/router.actions";
import {StringTMap} from "../common/object-maps";

type  filterPreparationTypes =
    |DrainFilterFailure
    |PauseFilterFailure
    |LoadFilterBlueprintFailure
    |LoadFilterConfigurationFailure
    |LoadFilterStateFailure
    |LoadAllDescriptorsForBlueprintFailureAction;

const filterPreparationFailure = [
    DRAIN_FILTER_FAILURE,
    PAUSE_FILTER_FAILURE,
    LOAD_FILTER_BLUEPRINT_FAILURE,
    LOAD_FILTER_CONFIGURATION_FAILURE,
    LOAD_FILTERSTATE_FAILURE,
    LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_FAILURE
];

@Injectable()
export class FiltersEffects {

    @Effect()
    public initializeLiveEditing$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
            const regex = /\/filter\/.*/g;
            const filterBlueprintId = this.getFilterBlueprintId(action as RouterNavigationAction);
            if (this.handleNavigation(regex, action as RouterNavigationAction)) {
                this.store.dispatch(new ResetAction());
                return this.blueprintService.getBlueprint(filterBlueprintId).pipe(
                    map((blueprint: Blueprint) => new LoadFilterBlueprintSuccess(blueprint),
                        catchError((cause: any) => of(new LoadFilterBlueprintFailure(cause)))
                    ));
            }
            else {
                return of();
            }
        })
    );

    @Effect()
    public navigateToLiveEditing$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_BLUEPRINT_SUCCESS),
        map((action) => (action as LoadFilterBlueprintSuccess)),
        concatMap((payload) => [
            new LoadDescriptorForBlueprint(payload.blueprint.descriptor.uuid),
            new PauseFilterAction(payload.blueprint.ref.uuid, true),
            new DrainFilterAction(payload.blueprint.ref.uuid, true),
            new LoadFilterConfigurationAction(payload.blueprint.configuration.uuid),
            new LoadFilterStateAction(payload.blueprint.ref.uuid, 10)
        ])
    );

    @Effect()
    public pauseFilter$: Observable<Action> = this.actions$.pipe(
        ofType(PAUSE_FILTER),
        map((action) => (action as PauseFilterAction)),
        concatMap(action => {
            return this.filterControllerService.pauseFilter(action.filterId, action.pause).pipe(
                map((state: ResourceInstanceState) => new PauseFilterSuccess(state)),
                catchError((cause) => of(new PauseFilterFailure(cause))))
        }));


    @Effect()
    public drainFilter: Observable<Action> = this.actions$.pipe(
        ofType(DRAIN_FILTER),
        map((action) => (action as DrainFilterAction)),
        concatMap(action => {
            return this.filterControllerService.drainFilter(action.filterId, action.drain).pipe(
                map((state: ResourceInstanceState) => new DrainFilterSuccess(state),
                    catchError((cause) => of(new DrainFilterFailure(cause)))
                ));
        })
    );

    @Effect()
    public loadFilterConfiguration$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_CONFIGURATION),
        map((action) => (action as LoadFilterConfigurationAction)),
        concatMap(action => {
            return this.configurationService.getConfiguration(action.filterId).pipe(
                map((conf: Configuration) => new LoadFilterConfigurationSuccess(conf, action.filterId),
                    catchError((cause) => of(new LoadFilterConfigurationFailure(cause)))
                ));
        })
    );

    @Effect()
    public loadFilterState$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTERSTATE),
        map((action) => (action as LoadFilterStateAction)),
        concatMap(action => {
            return this.filterControllerService.getState(action.filterId).pipe(
                map((state: ResourceInstanceState) => new LoadFilterStateSuccess(state),
                    catchError((cause) => of(new LoadFilterStateFailure(cause)))
                ));
        })
    );


    @Effect() public loadFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_DESCRIPTOR_FOR_BLUEPRINT),
        map((action) => (action as LoadDescriptorForBlueprint)),
        switchMap((action) =>
            this.descriptorService.getDescriptorById(action.uuid).pipe(
                map((descriptor: Descriptor) => new LoadDescriptorForBlueprintSuccess(descriptor)),
                catchError((cause) => of(new LoadAllDescriptorsForBlueprintFailureAction(cause)))
            )
        )
    );

    @Effect() public resolveFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_DESCRIPTOR_FOR_BLUEPRINT_SUCCESS),
        map(action => (action as LoadDescriptorForBlueprintSuccess).descriptor),
        map((descriptor) => {
            let resolvedDescriptor = this.descriptorResolver.resolveDescriptor(descriptor);
            return new ResolvedDescriptorForBlueprintSuccess(resolvedDescriptor);
        }));

    @Effect()
    public insertDatasets$: Observable<Action> = this.actions$.pipe(
        ofType(RECONFIGURE_FILTER_SUCCESS),
        withLatestFrom(
            this.store.pipe(select(selectCurrentBlueprint)),
            this.store.pipe(select(selectInputDatasets))),
        switchMap(([_, blueprint, datasets]) => {
                return this.filterControllerService.insertDatasets(blueprint.ref.uuid, datasets).pipe(
                    map((state: ResourceInstanceState) => new InsertDatasetsSuccess(state)),
                    catchError((cause: any) => of(new InsertDatasetsFailure(cause)))
                )
            }
        )
    );

    @Effect()
    public fireExtractDatasetsWhenInsertDatasetsSuccessAction: Observable<Action> = this.actions$.pipe(
        ofType(INSERT_DATASETS_SUCCESS),
        withLatestFrom(this.store.pipe(select(selectCurrentBlueprint)), this.store.pipe(select(selectInputDatasets))),
        switchMap(([_, blueprint, models]) => of(new ExtractDatasetsAction(blueprint.ref.uuid, models.length)))
    );

    @Effect()
    public initialExtract: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTERSTATE),
        map((action) => (action as LoadFilterStateAction)),
        switchMap((action) => {
            return forkJoin(
                this.filterControllerService.extractDatasets(action.filterId, action.amount, "before"),
                this.filterControllerService.extractDatasets(action.filterId, action.amount, "after")
            ).pipe(
                map((data: Dataset[][]) => new InitialExtractSuccess(data[0], data[1])),
                catchError((cause: any) => of(new ExtractDatasetsFailure(cause))))
        }),
    );

    @Effect()
    public extractDatasets: Observable<Action> = this.actions$.pipe(
        ofType(EXTRACT_DATASETS),
        map((action) => (action as ExtractDatasetsAction)),
        switchMap((action) => {
            return this.filterControllerService.extractDatasets(action.filterId, action.amount, "after").pipe(
                map((datasets: Dataset[]) => new ExtractDatasetsResultSuccess(datasets)),
                catchError((cause: any) => of(new ExtractDatasetsFailure(cause)))
            );
        }),
    );

    @Effect()
    public updateFilterConfiguration$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_FILTER_CONFIGURATION),
        map((action) => (action as UpdateFilterConfiguration)),
        withLatestFrom(this.store.pipe(select(selectCurrentBlueprint))),
        switchMap(([action, blueprint]) => {
            return this.filterControllerService.updateConfig(action.configuration, blueprint.ref.uuid).pipe(
                map((state: ResourceInstanceState) => new ReconfigureFilterSuccess(state)),
                catchError((cause: any) => of(new ReconfigureFilterFailure(cause)))
            );
        })
    );

    @Effect()
    public restoreFilterConfiguration$: Observable<Action> = this.actions$.pipe(
        ofType(RESTORE_FILTER_CONFIGURATION),
        map((action) => (action as RestoreFilterConfiguration)),
        withLatestFrom(this.store.pipe(select(selectInitialConfiguration)), this.store.pipe(select(selectCurrentBlueprint))),
        mergeMap(([action, initialConfiguration, blueprint]) => {
            return this.filterControllerService.updateConfig(initialConfiguration, blueprint.ref.uuid).pipe(
                map((state: ResourceInstanceState) => new ReconfigureFilterSuccess(state)),
                catchError((cause: any) => of(new ReconfigureFilterFailure(cause)))
            );

        })
    );

    @Effect()
    public loadAllPipelines$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_ALL_PIPELINES_FOR_REDIRECT),
        mergeMap(_ => {
            return this.blueprintService.loadAllPipelineBlueprints().pipe(
                withLatestFrom(this.store.pipe(select(selectCurrentBlueprint))),
                map(([pipelines, blueprint]) => new RouterActions.Go({
                    path: ["pipelines/" + this.getMatchingPipeline(pipelines, blueprint), {}],
                    query: {},
                    extras: {}
                })),
                catchError((cause: any) => of(new NaviagatetoPipelyFailure(cause))));
        })
    );

    @Effect()
    public overwriteConfiguration$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_CONFIGURATION_IN_BACKEND),
        map((action) => (action as UpdateConfigurationInBackend).configuration),
        mergeMap((config) => this.configurationService.putConfiguration(config)),
        map(_ => new OverwriteSuccess()),
        catchError((cause) => of(new ReconfigureFilterFailure(cause))
        ));

    // SnackBar

    @Effect() public reconfigureFilterFailure$: Observable<Action> = this.actions$.pipe(
        ofType(RECONFIGURE_FILTER_FAILURE),
        map(() => new SnackbarOpen({
            message: "An error occured while appliying the configuration.",
            action: 'Failed',
            config: {
                horizontalPosition: "center",
                verticalPosition: "top"
            }
        }))
    );

    @Effect() public navigateToPipelySnackBar$: Observable<Action> = this.actions$.pipe(
        ofType(NAVIAGATE_TO_PIPELY_FAILURE),
        map(() => new SnackbarOpen({
            message: "No running pipeline for this filter was found.",
            action: 'Failed',
            config: {
                horizontalPosition: "center",
                verticalPosition: "top"
            }
        }))
    );

    @Effect() public reconfigureFilterSuccess$: Observable<Action> = this.actions$.pipe(
        ofType(RECONFIGURE_FILTER_SUCCESS),
        map(() => new SnackbarOpen({
            message: "Your settings were tested.",
            action: 'Success',
            config: {
                horizontalPosition: "center",
                verticalPosition: "top"
            }
        }))
    );

    @Effect() public applyConfiguration$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_CONFIGURATION_IN_BACKEND),
        map(() => new SnackbarOpen({
            message: "Configuration has been applied and saved in the pipeline.",
            action: 'Success',
            config: {
                horizontalPosition: "center",
                verticalPosition: "top"
            }
        }))
    );

    @Effect() public handleErrors$: Observable<Action> = this.actions$.pipe(
        ofType<filterPreparationTypes>(...filterPreparationFailure),
        map(() => new SnackbarOpen({
            message: "Filter could not be prepared for Live-Editing. Please check if the resource is running.",
            action: 'Failure',
            config: {
                horizontalPosition: "center",
                verticalPosition: "top"
            }
        }))
    );

    @Effect() public insertFailure$: Observable<Action> = this.actions$.pipe(
        ofType(INSERT_DATASETS_FAILURE),
        map(() => new SnackbarOpen({
            message: "Datasets could not be inserted in the filter.",
            action: 'Failure',
            config: {
                horizontalPosition: "center",
                verticalPosition: "top"
            }
        }))
    );

    @Effect() public extractFailure$: Observable<Action> = this.actions$.pipe(
        ofType(EXTRACT_DATASETS_FAILURE),
        map(() => new SnackbarOpen({
            message: "Datasets could not be extracted from filter.",
            action: 'Failure',
            config: {
                horizontalPosition: "center",
                verticalPosition: "top"
            }
        }))
    );

    getMatchingPipeline(pipelines: StringTMap<PipelineBlueprint>, blueprint: Blueprint) {
        let pipelineList = Object.values(pipelines);
        let result: PipelineBlueprint = undefined;
        console.log("sdfsdaffsdf" , blueprint);
        pipelineList.map(pipeline => {
            pipeline.blueprints.forEach(bp => {
                if (bp.uuid === blueprint.ref.uuid) {
                    result = pipeline;
                }
            })
        });
        console.log("Result is", result.ref.uuid);
        return result.ref.uuid;
    }


    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private filterControllerService: FilterControllerService,
                private configurationService: ConfigurationService,
                private descriptorService: DescriptorService,
                private descriptorResolver: DescriptorResolverService,
                private pipelineService: PipelineService,
                private blueprintService: BlueprintService) {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        return regEx.test(action.payload.event.url);
    }

    private getFilterBlueprintId(action: RouterNavigationAction): string {
        const url = action.payload.event.url;
        return url.substring(url.lastIndexOf("/") + 1, url.length)
    }
}