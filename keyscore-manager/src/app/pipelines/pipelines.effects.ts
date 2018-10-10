import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {Action, select, Store} from "@ngrx/store";
import {forkJoin, Observable, of} from "rxjs";
import {concat, concatMap, delay, skip, tap, withLatestFrom} from "rxjs/internal/operators";
import {catchError, combineLatest, map, mergeMap, switchMap} from "rxjs/operators";
import {AppState} from "../app.component";
import {selectAppConfig} from "../app.config";
import {selectRefreshTime} from "../common/loading/loading.reducer";
import {
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
    LoadAllPipelineInstancesAction,
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
    UPDATE_PIPELINE,
    UPDATE_PIPELINE_FAILURE,
    UPDATE_PIPELINE_SUCCESS,
    UpdatePipelineAction,
    UpdatePipelineFailureAction,
    UpdatePipelineSuccessAction,
} from "./pipelines.actions";
import {PipelineInstance} from "../models/pipeline-model/PipelineInstance";
import {getEditingPipeline, getPipelinePolling} from "./pipelines.reducer";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {BlueprintService} from "../services/rest-api/BlueprintService";
import {Blueprint, PipelineBlueprint} from "../models/blueprints/Blueprint";
import {Configuration} from "../models/common/Configuration";
import {Descriptor} from "../models/descriptors/Descriptor";
import {DescriptorResolverService} from "../services/descriptor-resolver.service";
import {StringTMap} from "../common/object-maps";
import {SnackbarOpen} from "../common/snackbar/snackbar.actions";
import {ConfigurationService} from "../services/rest-api/ConfigurationService";
import {DescriptorService} from "../services/rest-api/DescriptorService";

@Injectable()
export class PipelinesEffects {
    @Effect() public editPipeline$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        withLatestFrom(this.store.pipe(select(getEditingPipeline))),
        mergeMap(([action, editingPipeline]) => {
            const regex = /\/pipelines\/.+/g;
            if (this.handleNavigation(regex, action as RouterNavigationAction)) {
                const id = this.getPipelineIdfromRouterAction(action as RouterNavigationAction);
                if (!editingPipeline || (editingPipeline && editingPipeline.pipelineBlueprint.ref.uuid !== id)) {
                    return of(new EditPipelineAction(id));
                }
            }
            return of();
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
        map(action => (action as UpdatePipelineAction).pipeline),
        mergeMap(pipeline => {
            return forkJoin(
                ...pipeline.blueprints.map(blueprint =>
                    this.blueprintService.putBlueprint(blueprint)
                ),
                ...pipeline.configurations.map(configuration =>
                    this.configurationService.putConfiguration(configuration)
                ),
                this.blueprintService.putPipelineBlueprint(pipeline.pipelineBlueprint)
            ).pipe(map(data => new UpdatePipelineSuccessAction(pipeline)),
                catchError(cause => of(new UpdatePipelineFailureAction(cause, pipeline))))
        })
    );

    @Effect() public updatePipelineSuccess$:Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_PIPELINE_SUCCESS),
        map(() => new SnackbarOpen({
            message:"Successfully saved all configurations!",
            action:'Success',
            config:{
                horizontalPosition:"center",
                verticalPosition:"top"
            }
        }))
    );

    @Effect() public updatePipelineFailure$:Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_PIPELINE_FAILURE),
        map(() => new SnackbarOpen({
            message:"An error occured while saving the configurations.",
            action:'Failed',
            config:{
                horizontalPosition:"center",
                verticalPosition:"top"
            }
        }))
    );

    @Effect() public deletePipeline$: Observable<Action> = this.actions$.pipe(
        ofType(DELETE_PIPELINE),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([action, config]) => {
            const pipelineUrl: string = config.getString("keyscore.frontier.base-url") + "/pipeline/configuration/";
            const pipelineId: string = (action as DeletePipelineAction).id;
            return this.http.delete(pipelineUrl + pipelineId).pipe(
                map((data) => new DeletePipelineSuccessAction(pipelineId)),
                catchError((cause: any) => of(new DeletePipelineFailureAction(cause, pipelineId)))
            );
        })
    );

    @Effect() public loadPipelineInstances$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_PIPELINEBLUEPRINTS_SUCCESS),
        withLatestFrom(this.store.select(selectAppConfig)),
        withLatestFrom(this.store.select(selectRefreshTime)),
        concatMap(([[action, config], refreshTime]) =>
            this.http.get(config.getString("keyscore.frontier.base-url") + "/pipeline/instance/*").pipe(
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

    @Effect() public loadPipelineBlueprints$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_PIPELINEBLUEPRINTS),
        mergeMap(_ => {
            return this.blueprintService.getAllPipelineBlueprints().pipe(
                map((blueprints) => new LoadPipelineBlueprintsSuccess(Object.values(blueprints))),
                catchError((cause) => of(new LoadPipelineBlueprintsFailure(cause)))
            );
        })

    );

    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private http: HttpClient,
                private blueprintService: BlueprintService,
                private configurationService: ConfigurationService,
                private descriptorService: DescriptorService,
                private descriptorResolver: DescriptorResolverService) {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        return regEx.test(action.payload.event.url);

    }

    private getPipelineIdfromRouterAction(action: RouterNavigationAction) {
        return action.payload.routerState.root.firstChild.firstChild.url[0].path;
    }
}
