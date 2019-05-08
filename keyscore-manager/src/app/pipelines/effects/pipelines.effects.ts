import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {ROUTER_NAVIGATION, RouterNavigationAction} from "@ngrx/router-store";
import {Action, select, Store} from "@ngrx/store";
import {forkJoin, Observable, of} from "rxjs";
import {concat, concatMap, delay, skip, tap, withLatestFrom} from "rxjs/internal/operators";
import {catchError, combineLatest, map, mergeMap, switchMap} from "rxjs/operators";
import {AppState} from "../../app.component";
import {selectAppConfig} from "../../app.config";
import {selectRefreshTime} from "../../common/loading/loading.reducer";
import {
    CHECK_IS_PIPELINE_RUNNING,
    CheckIsPipelineRunning,
    ConfigurationsForBlueprintId,
    CreatedPipelineAction,
    DELETE_PIPELINE,
    DeletePipelineAction,
    DeletePipelineFailureAction,
    DeletePipelineSuccessAction,
    EDIT_PIPELINE,
    EditPipelineAction,
    EditPipelineFailureAction,
    EditPipelineSuccessAction,
    LOAD_EDIT_PIPELINE_BLUEPRINTS,
    LOAD_EDIT_PIPELINE_CONFIG,
    LOAD_FILTER_DESCRIPTORS,
    LOAD_FILTER_DESCRIPTORS_SUCCESS,
    LOAD_PIPELINEBLUEPRINTS,
    LOAD_PIPELINEBLUEPRINTS_SUCCESS,
    LoadAllPipelineInstancesFailureAction,
    LoadAllPipelineInstancesSuccessAction,
    LoadEditBlueprintsAction,
    LoadEditPipelineConfigAction,
    LoadFilterDescriptorsFailureAction,
    LoadFilterDescriptorsSuccessAction,
    LoadPipelineBlueprints,
    LoadPipelineBlueprintsFailure,
    LoadPipelineBlueprintsSuccess,
    ResolveFilterDescriptorSuccessAction,
    RUN_PIPELINE,
    RUN_PIPELINE_FAILURE,
    RUN_PIPELINE_SUCCESS,
    RunPipelineAction,
    RunPipelineFailureAction,
    RunPipelineSuccessAction,
    TRIGGER_FILTER_RESET,
    TriggerFilterResetAction,
    TriggerFilterResetFailure,
    UPDATE_PIPELINE,
    UPDATE_PIPELINE_FAILURE,
    UPDATE_PIPELINE_SUCCESS,
    UpdatePipelineAction,
    UpdatePipelineFailureAction,
    UpdatePipelineSuccessAction,
} from "../actions/pipelines.actions";
import {
    PipelineInstance,
    ResolvedFilterDescriptor,
    Blueprint,
    PipelineBlueprint,
    Configuration,
    Descriptor,
    Health
} from "keyscore-manager-models";
import {DescriptorResolverService} from "../../services/descriptor-resolver.service";
import {StringTMap} from "../../common/object-maps";
import {SnackbarOpen} from "../../common/snackbar/snackbar.actions";
import {ConfigurationService} from "../../services/rest-api/ConfigurationService";
import {DescriptorService} from "../../services/rest-api/DescriptorService";
import {PipelineService} from "../../services/rest-api/PipelineService";
import {FilterControllerService} from "../../services/rest-api/FilterController.service";
import {getPipelinePolling, selectIsCreating} from "../index";
import {BlueprintService} from "../../services/rest-api/BlueprintService";

@Injectable()
export class PipelinesEffects {
    @Effect() public editPipeline$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        map(action => action as RouterNavigationAction),
        withLatestFrom(this.store.pipe(select(selectIsCreating))),
        map(([action, isCreating]) => {
            const regex = /\/pipelines\/.+/g;
            if (this.handleNavigation(regex, action as RouterNavigationAction)) {
                const id = this.getPipelineIdfromRouterAction(action as RouterNavigationAction);
                if (!isCreating) {
                    return new EditPipelineAction(id);
                }
            }
            return new CreatedPipelineAction();
        })
    );

    @Effect() public loadEditPipelineBlueprint$: Observable<Action> = this.actions$.pipe(
        ofType(EDIT_PIPELINE),
        map((action) => (action as EditPipelineAction).id),
        switchMap((pipelineId) => {
            return this.blueprintService.getPipelineBlueprint(pipelineId).pipe(
                map((pipelineBlueprint: PipelineBlueprint) => {
                    if (pipelineBlueprint === null) {
                        return new EditPipelineFailureAction({
                            status: "404",
                            message: `Sorry we were not able to find pipeline ${pipelineId}`
                        });
                    }
                    if (pipelineBlueprint.blueprints.length > 0) {
                        return new LoadEditBlueprintsAction(pipelineBlueprint);
                    }
                    else {
                        return new EditPipelineSuccessAction(pipelineBlueprint, [], []);
                    }
                }),
                catchError((cause: any) =>
                    of(new EditPipelineFailureAction(cause))
                )
            );
        })
    );


    @Effect() public loadEditBlueprints$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_EDIT_PIPELINE_BLUEPRINTS),
        map(action => (action as LoadEditBlueprintsAction)),
        switchMap(action => {
            return forkJoin(
                ...action.pipelineBlueprint.blueprints.map(blueprintRef => this.blueprintService.getBlueprint(blueprintRef.uuid))
            ).pipe(map((blueprints: Blueprint[]) => new LoadEditPipelineConfigAction(action.pipelineBlueprint, blueprints)),
                catchError(cause => of(new EditPipelineFailureAction(cause))))
        })
    );

    @Effect() public loadEditConfigs$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_EDIT_PIPELINE_CONFIG),
        map(action => (action as LoadEditPipelineConfigAction)),
        switchMap(action => {
            return forkJoin(
                ...action.blueprints.map(blueprint => this.configurationService.getConfiguration(blueprint.configuration.uuid))
            ).pipe(map((configurations: Configuration[]) =>
                    new EditPipelineSuccessAction(action.pipelineBlueprint, action.blueprints, configurations)),
                catchError(cause => of(new EditPipelineFailureAction(cause)))
            )

        })
    );

    @Effect() public loadFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_DESCRIPTORS),
        switchMap((action) =>
            this.descriptorService.getAllDescriptors().pipe(
                map((descriptorsMap: StringTMap<Descriptor>) => new LoadFilterDescriptorsSuccessAction(Object.values(descriptorsMap))),
                catchError((cause) => of(new LoadFilterDescriptorsFailureAction(cause)))
            )
        )
    );

    @Effect() public resolveFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_DESCRIPTORS_SUCCESS),
        map(action => (action as LoadFilterDescriptorsSuccessAction).descriptors),
        map(descriptors => {
            let resolvedDescriptors: ResolvedFilterDescriptor[] = descriptors.map(descriptor =>
                this.descriptorResolver.resolveDescriptor(descriptor)
            );
            return new ResolveFilterDescriptorSuccessAction(resolvedDescriptors);
        })
    );

    @Effect() public updatePipeline$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_PIPELINE),
        map(action => (action as UpdatePipelineAction)),
        mergeMap(action => {
            return forkJoin(
                this.blueprintService.putPipelineBlueprint(action.pipeline.pipelineBlueprint),
                ...action.pipeline.blueprints.map(blueprint =>
                    this.blueprintService.putBlueprint(blueprint)
                ),
                ...action.pipeline.configurations.map(configuration =>
                    this.configurationService.putConfiguration(configuration)
                )
            ).pipe(map(data => new UpdatePipelineSuccessAction(action.pipeline, action.runAfterUpdate)),
                catchError(cause => of(new UpdatePipelineFailureAction(cause, action.pipeline))))
        })
    );

    @Effect() public updatePipelineSuccess$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_PIPELINE_SUCCESS),
        map(action => (action as UpdatePipelineSuccessAction)),
        map((action) => {
            if (action.runPipeline) {
                return new RunPipelineAction(action.pipeline.pipelineBlueprint.ref);
            } else {
                return new SnackbarOpen({
                    message: "Successfully saved all configurations!",
                    action: 'Success',
                    config: {
                        horizontalPosition: "center",
                        verticalPosition: "top"
                    }
                })
            }
        })
    );

    @Effect() public updatePipelineFailure$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_PIPELINE_FAILURE),
        map(() => new SnackbarOpen({
            message: "An error occured while saving the configurations.",
            action: 'Failed',
            config: {
                horizontalPosition: "center",
                verticalPosition: "top"
            }
        }))
    );

    @Effect() public runPipeline$: Observable<Action> = this.actions$.pipe(
        ofType(RUN_PIPELINE),
        map(action => (action as RunPipelineAction).blueprintRef),
        switchMap((blueprintRef) => {
            return this.pipelineService.runPipeline(blueprintRef).pipe(
                map(data => new CheckIsPipelineRunning(blueprintRef, 100)),
                catchError(cause => of(new RunPipelineFailureAction(cause, blueprintRef)))
            )
        })
    );

    @Effect() public runPipelineFailure$: Observable<Action> = this.actions$.pipe(
        ofType(RUN_PIPELINE_FAILURE),
        map(() => new SnackbarOpen({
            message: "An error occured while trying to run the pipeline.",
            action: 'Failed',
            config: {
                horizontalPosition: "center",
                verticalPosition: "top"
            }
        }))
    );

    @Effect() public runPipelineSuccess$: Observable<Action> = this.actions$.pipe(
        ofType(RUN_PIPELINE_SUCCESS),
        map(() => new SnackbarOpen({
            message: "Your Pipeline is running now!",
            action: 'Success',
            config: {
                horizontalPosition: "center",
                verticalPosition: "top"
            }
        }))
    );


    @Effect() public deletePipeline$: Observable<Action> = this.actions$.pipe(
        ofType(DELETE_PIPELINE),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([action, config]) => {
            const pipelineUrl: string = config.getString("keyscore.frontier.base-url") + "/pipeline/initialConfiguration/";
            const pipelineId: string = (action as DeletePipelineAction).id;
            return this.http.delete(pipelineUrl + pipelineId).pipe(
                map((data) => new DeletePipelineSuccessAction(pipelineId)),
                catchError((cause: any) => of(new DeletePipelineFailureAction(cause, pipelineId)))
            );
        })
    );

    @Effect() public loadPipelineInstances$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_PIPELINEBLUEPRINTS_SUCCESS),
        withLatestFrom(this.store.select(selectRefreshTime)),
        concatMap(([action, refreshTime]) =>
            this.pipelineService.loadAllInstances().pipe(
                concat(of("").pipe(
                    delay(refreshTime > 0 ? refreshTime : 0),
                    withLatestFrom(this.store.select(getPipelinePolling)),
                    tap(([_, polling]) => {
                        if (polling && refreshTime > 0) {
                            this.store.dispatch(new LoadPipelineBlueprints());
                        }
                    }), skip(1))),
                map((data: PipelineInstance[]) => new LoadAllPipelineInstancesSuccessAction(data)),
                catchError((cause) => of(new LoadAllPipelineInstancesFailureAction(cause)))
            )
        )
    );

    @Effect() public checkIsPipelineRunning$: Observable<Action> = this.actions$.pipe(
        ofType(CHECK_IS_PIPELINE_RUNNING),
        map(action => action as CheckIsPipelineRunning),
        switchMap(action => {
            return this.pipelineService.loadInstance(action.pipelineRef.uuid).pipe(
                map((data: PipelineInstance) => {
                    if (data.health === Health.Green) {
                        return new RunPipelineSuccessAction(action.pipelineRef);
                    } else if (action.timeToLive > 0) {
                        return new CheckIsPipelineRunning(action.pipelineRef, action.timeToLive - 1);
                    }
                    return new RunPipelineFailureAction(null, action.pipelineRef);

                }),
                catchError((cause) => of(new RunPipelineFailureAction(cause, action.pipelineRef)))
            )
        })
    );

    @Effect() public loadPipelineBlueprints$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_PIPELINEBLUEPRINTS),
        mergeMap(_ => {
            return this.blueprintService.loadAllPipelineBlueprints().pipe(
                map((blueprints) => new LoadPipelineBlueprintsSuccess(Object.values(blueprints))),
                catchError((cause) => of(new LoadPipelineBlueprintsFailure(cause)))
            );
        })
    );

    @Effect() TriggerFilterResetAction$: Observable<Action> = this.actions$.pipe(
        ofType(TRIGGER_FILTER_RESET),
        map((action) => (action as TriggerFilterResetAction)),
        switchMap(action => {
            return this.blueprintService.getPipelineBlueprint(action.uuid).pipe(
                map((blueprint) => new ConfigurationsForBlueprintId(blueprint.blueprints)),
                catchError((cause) => of(new TriggerFilterResetFailure(cause)))
            );
        })
    );

    // @Effect() ResetFilterStatusAction$: Observable<Action> = this.actions$.pipe(
    //     ofType(CONFIGS_FOR_BLUEPRINT),
    //     map((action) => (action as ConfigurationsForBlueprintId)),
    //     mergeMap(action =>  {
    //
    //        return this.filterControllerService.pauseFilter(action.blueprints$[0].uuid, false).pipe(
    //            map((result) => new DeletePipelineSuccessAction("!"),
    //                catchError(_ => of({stageType: "NOOP"})))
    //        )
    //     })
    // );

    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private http: HttpClient,
                private blueprintService: BlueprintService,
                private configurationService: ConfigurationService,
                private descriptorService: DescriptorService,
                private descriptorResolver: DescriptorResolverService,
                private filterControllerService: FilterControllerService,
                private pipelineService: PipelineService) {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        return regEx.test(action.payload.event.url);

    }

    private getPipelineIdfromRouterAction(action: RouterNavigationAction) {
        return action.payload.routerState.root.firstChild.firstChild.url[0].path;
    }
}
