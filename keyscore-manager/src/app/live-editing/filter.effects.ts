import {Injectable} from "@angular/core";
import {AppState} from "../app.component";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Observable, of} from "rxjs/index";
import {ROUTER_NAVIGATION, RouterNavigationAction} from "@ngrx/router-store";
import {catchError, combineLatest, concatMap, map, mergeMap, withLatestFrom} from "rxjs/internal/operators";
import {Action, select, Store} from "@ngrx/store";
import {
    DRAIN_FILTER,
    DrainFilterAction,
    DrainFilterFailure,
    DrainFilterSuccess,
    EXTRACT_DATASETS,
    ExtractDatasetsAction,
    ExtractDatasetsFailure,
    ExtractDatasetsInitialSuccess,
    ExtractDatasetsResultSuccess,
    INSERT_DATASETS_SUCCESS,
    InsertDatasetsFailure,
    InsertDatasetsSuccess,
    LOAD_DESCRIPTOR_FOR_BLUEPRINT,
    LOAD_DESCRIPTOR_FOR_BLUEPRINT_SUCCESS,
    LOAD_FILTER_BLUEPRINT_SUCCESS,
    LOAD_FILTER_CONFIGURATION,
    LOAD_FILTER_CONFIGURATION_SUCCESS,
    LOAD_FILTERSTATE,
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
    PAUSE_FILTER,
    PauseFilterAction,
    PauseFilterFailure,
    PauseFilterSuccess,
    RECONFIGURE_FILTER_SUCCESS,
    ReconfigureFilterFailure,
    ReconfigureFilterSuccess,
    ResolvedDescriptorForBlueprintSuccess
} from "./filters.actions";
import {RestCallService} from "../services/rest-api/rest-call.service";
import {Configuration} from "../models/common/Configuration";
import {Blueprint} from "../models/blueprints/Blueprint";
import {ResourceInstanceState} from "../models/filter-model/ResourceInstanceState";
import {Descriptor} from "../models/descriptors/Descriptor";
import {switchMap} from "rxjs/operators";
import {LoadAllDescriptorsForBlueprintFailureAction} from "../resources/resources.actions";
import {DescriptorResolverService} from "../services/descriptor-resolver.service";
import {
    selectConfiguration,
    selectCurrentBlueprintId,
    selectExtractedDatasets,
    selectUpdateConfigurationFlag
} from "./filter.reducer";
import {Dataset} from "../models/dataset/Dataset";


@Injectable()
export class FiltersEffects {

    @Effect()
    public initializeLiveEditing$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
            const regex = /\/filter\/.*/g;
            const filterBlueprintId = this.getFilterBlueprintId(action as RouterNavigationAction);
            if (this.handleNavigation(regex, action as RouterNavigationAction)) {
                return this.restCallService.getBlueprint(filterBlueprintId).pipe(
                  map((blueprint: Blueprint) => new LoadFilterBlueprintSuccess(blueprint),
                      catchError((cause: any) =>  of(new LoadFilterBlueprintFailure(cause)))
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
            new LoadFilterStateAction(payload.blueprint.ref.uuid)
        ])
    );

    @Effect()
    public pauseFilter$: Observable<Action> = this.actions$.pipe(
        ofType(PAUSE_FILTER),
        map((action) => (action as PauseFilterAction)),
        concatMap(action => {
            return this.restCallService.pauseFilter(action.filterId, action.pause).pipe(
                map((state: ResourceInstanceState) => new PauseFilterSuccess(state)),
                catchError((cause) => of(new PauseFilterFailure(cause))))
        }));


    @Effect()
    public drainFilter: Observable<Action> = this.actions$.pipe(
        ofType(DRAIN_FILTER),
        map((action) => (action as DrainFilterAction)),
        concatMap(action => {
            return this.restCallService.drainFilter(action.filterId, action.drain).pipe(
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
            return this.restCallService.getConfiguration(action.filterId).pipe(
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
            return this.restCallService.getResourceState(action.filterId).pipe(
                map((state: ResourceInstanceState) => new LoadFilterStateSuccess(state),
                    catchError((cause) => of(new LoadFilterStateFailure(cause)))
                ));
        })
    );


    @Effect() public loadFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_DESCRIPTOR_FOR_BLUEPRINT),
        map((action) => (action as LoadDescriptorForBlueprint)),
        switchMap((action) =>
            this.restCallService.getDescriptorById(action.uuid).pipe(
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
            this.store.pipe(select(selectCurrentBlueprintId)),
            this.store.pipe(select(selectExtractedDatasets))),
        switchMap(([_, id, datasets]) =>
           this.restCallService.insertDatasets(id, datasets).pipe(
                map((state: ResourceInstanceState) => new InsertDatasetsSuccess(state)),
                catchError((cause: any) => of(new InsertDatasetsFailure(cause)))
            )
        )
    );

    @Effect()
    public fireExtractDatasetsWhenInsertDatasetsSuccessAction: Observable<Action> = this.actions$.pipe(
        ofType(INSERT_DATASETS_SUCCESS),
        withLatestFrom(this.store.pipe(select(selectCurrentBlueprintId)), this.store.pipe(select(selectExtractedDatasets))),
        switchMap(([_, filterId, datasets] ) => of(new ExtractDatasetsAction(filterId, datasets.length)))
    );

    @Effect()
    public fireExtractDatasetsWhenLoadLiveEditEditingFilterSuccesAction: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_CONFIGURATION_SUCCESS),
        map((action) => (action as LoadFilterConfigurationSuccess)),
        switchMap((action) => {
            return this.restCallService.extractDatasets(action.filterId).pipe(
                map((datasets: Dataset[]) => new ExtractDatasetsInitialSuccess(datasets)),
                catchError((cause: any) => of(new ExtractDatasetsFailure(cause)))
            );
        }),
    );

    @Effect()
    public extractDatasets: Observable<Action> = this.actions$.pipe(
        ofType(EXTRACT_DATASETS),
        map((action) => (action as ExtractDatasetsAction)),
        switchMap((action) => {
            return this.restCallService.extractDatasets(action.filterId).pipe(
                map((datasets: Dataset[]) => new ExtractDatasetsResultSuccess(datasets)),
                catchError((cause: any) => of(new ExtractDatasetsFailure(cause)))
            );
        }),
    );

    @Effect()
    public updateConfiguration: Observable<Action> = this.actions$.pipe(
        combineLatest(this.store.pipe(select(selectConfiguration)), this.store.pipe(select(selectUpdateConfigurationFlag))),
        switchMap(([_, filterConfiguration, triggerCall]) => {
            if (triggerCall) {
                return this.restCallService.updateConfig(filterConfiguration).pipe(
                    map((state: ResourceInstanceState) => new ReconfigureFilterSuccess(state)),
                    catchError((cause: any) => of(new ReconfigureFilterFailure(cause)))
                );
            } else {
                return of();
            }
        })
    );



    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private restCallService: RestCallService,
                private descriptorResolver: DescriptorResolverService) {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        return regEx.test(action.payload.event.url);
    }

    private getFilterBlueprintId(action: RouterNavigationAction): string {
        const url = action.payload.event.url;
        return url.substring(url.lastIndexOf("/") + 1, url.length)
    }
}