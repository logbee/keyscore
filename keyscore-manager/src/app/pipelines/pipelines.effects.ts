import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {Action, Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable, of} from "rxjs";
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
    EditPipelineSuccessAction, LOAD_EDIT_PIPELINE_BLUEPRINTS, LoadEditBlueprintsAction,
    LOAD_ALL_PIPELINES,
    LOAD_FILTER_DESCRIPTORS,
    LoadAllPipelinesAction,
    LoadAllPipelinesFailureAction,
    LoadAllPipelinesSuccessAction,
    LoadFilterDescriptorsFailureAction,
    LoadFilterDescriptorsSuccessAction,
    UPDATE_PIPELINE,
    UpdatePipelineAction,
    UpdatePipelineFailureAction,
    UpdatePipelineSuccessAction, LoadEditPipelineConfigAction, LOAD_EDIT_PIPELINE_CONFIG,
} from "./pipelines.actions";
import {PipelineConfiguration} from "../models/pipeline-model/PipelineConfiguration";
import {PipelineInstance} from "../models/pipeline-model/PipelineInstance";
import {getPipelinePolling} from "./pipelines.reducer";
import {FilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {PipelineService} from "../services/rest-api/pipeline.service";
import {Blueprint, PipelineBlueprint} from "../models/blueprints/Blueprint";
import {Configuration} from "../models/common/Configuration";

@Injectable()
export class PipelinesEffects {
    @Effect() public editPipeline$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
            const regex = /\/pipelines\/.+/g;
            if (this.handleNavigation(regex, action as RouterNavigationAction)) {
                const id = this.getPipelineIdfromRouterAction(action as RouterNavigationAction);
                console.log("Reached:", id);
                return of(new EditPipelineAction(id));
            }
            return of();
        })
    );

    @Effect() public getEditPipelineBlueprint$: Observable<Action> = this.actions$.pipe(
        ofType(EDIT_PIPELINE),
        map((action) => (action as EditPipelineAction).id),
        switchMap((pipelineId) => {
            return this.pipelineService.getPipelineBlueprint(pipelineId).pipe(
                map((data: PipelineBlueprint) => new LoadEditBlueprintsAction(data, 0, [])),
                catchError((cause: any) => of(new EditPipelineFailureAction(pipelineId, cause)))
            );
        })
    );

    @Effect() public getBlueprints$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_EDIT_PIPELINE_BLUEPRINTS),
        map(action => (action as LoadEditBlueprintsAction)),
        mergeMap((action) => {
            return this.pipelineService.getBlueprint(action.pipelineBlueprint.blueprints[action.index].uuid).pipe(
                map((data: Blueprint) => {
                    action.blueprints.push(data);
                    if (action.index < action.pipelineBlueprint.blueprints.length) {
                        return new LoadEditBlueprintsAction(action.pipelineBlueprint, action.index + 1, action.blueprints);
                    }
                    else {
                        return new LoadEditPipelineConfigAction(
                            action.pipelineBlueprint,
                            0,
                            action.blueprints,
                            []
                        );
                    }
                }),
                catchError((cause: any) =>
                    of(new EditPipelineFailureAction(action.pipelineBlueprint.ref.uuid, cause)))
            )
        })
    );

    @Effect() public getConfigurations$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_EDIT_PIPELINE_CONFIG),
        map(action => (action as LoadEditPipelineConfigAction)),
        mergeMap(action => {
            return this.pipelineService.getConfiguration(action.blueprints[action.index].configuration.uuid).pipe(
                map((data: Configuration) => {
                    action.configurations.push(data);
                    if (action.index < action.blueprints.length) {
                        return new LoadEditPipelineConfigAction(
                            action.pipelineBlueprint,
                            action.index + 1,
                            action.blueprints,
                            action.configurations
                        );
                    }
                    else {
                        return new EditPipelineSuccessAction(action.pipelineBlueprint, action.blueprints, action.configurations);
                    }
                }),
                catchError((cause: any) =>
                    of(new EditPipelineFailureAction(action.pipelineBlueprint.ref.uuid, cause)))
            )
        })
    );


    @Effect() public updatePipeline$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_PIPELINE),
        map((action) => (action as UpdatePipelineAction).pipeline),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([pipeline, config]) => {
            const pipelineUrl: string = config.getString("keyscore.frontier.base-url") + "/pipeline/configuration";
            return this.http.put(pipelineUrl, pipeline, {
                headers: new HttpHeaders().set("Content-Type", "application/json"),
                responseType: "text"
            }).pipe(
                map((data) => new UpdatePipelineSuccessAction(pipeline)),
                catchError((cause: any) => of(new UpdatePipelineFailureAction(cause, pipeline)))
            );
        })
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

    @Effect() public loadFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_DESCRIPTORS),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([action, config]) =>
            this.http.get(config.getString(
                "keyscore.frontier.base-url") + "/descriptors?language=" + this.translate.currentLang).pipe(
                map((data: FilterDescriptor[]) => new LoadFilterDescriptorsSuccessAction(data)),
                catchError((cause) => of(new LoadFilterDescriptorsFailureAction(cause)))
            )
        )
    );

    @Effect() public loadPipelineInstances$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_ALL_PIPELINES),
        withLatestFrom(this.store.select(selectAppConfig)),
        withLatestFrom(this.store.select(selectRefreshTime)),
        concatMap(([[action, config], refreshTime]) =>
            this.http.get(config.getString("keyscore.frontier.base-url") + "/pipeline/instance/*").pipe(
                concat(of("").pipe(
                    delay(refreshTime > 0 ? refreshTime : 0),
                    withLatestFrom(this.store.select(getPipelinePolling)),
                    tap(([_, polling]) => {
                        if (polling && refreshTime > 0) {
                            this.store.dispatch(new LoadAllPipelinesAction());
                        }
                    }), skip(1))),
                map((data: PipelineInstance[]) => new LoadAllPipelinesSuccessAction(data)),
                catchError((cause) => of(new LoadAllPipelinesFailureAction(cause)))
            )
        )
    );

    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private http: HttpClient,
                private pipelineService: PipelineService,
                private translate: TranslateService) {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        console.log("URL: ", action.payload.event.url);
        return regEx.test(action.payload.event.url);

    }

    private getPipelineIdfromRouterAction(action: RouterNavigationAction) {
        return action.payload.routerState.root.firstChild.firstChild.url[0].path;
    }
}
