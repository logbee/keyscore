import {catchError, combineLatest, map, mergeMap, switchMap} from "rxjs/operators";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {Observable, of} from "rxjs";
import {ROUTER_NAVIGATION} from '@ngrx/router-store'
import {Injectable} from "@angular/core";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {
    DELETE_PIPELINE,
    DeletePipelineAction,
    DeletePipelineFailureAction,
    DeletePipelineSuccessAction, EDIT_PIPELINE, EDIT_PIPELINE_FAILURE,
    EditPipelineAction, EditPipelineFailureAction, EditPipelineSuccessAction,
    LOAD_FILTER_DESCRIPTORS,
    LoadFilterDescriptorsFailureAction,
    LoadFilterDescriptorsSuccessAction,
    UPDATE_PIPELINE, UPDATE_PIPELINE_BLOCKLY,
    UpdatePipelineAction,
    UpdatePipelineFailureAction,
    UpdatePipelineSuccessAction, UpdatePipelineWithBlocklyAction
} from "./pipelines.actions";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {AppState} from "../app.component";
import {selectAppConfig} from "../app.config";
import {FilterDescriptor, getFilterDescriptors, PipelineConfiguration} from "./pipelines.model";
import {TranslateService} from "@ngx-translate/core";
import {toInternalPipelineConfig, toPipelineConfiguration} from "../util";
import {Go} from "../router/router.actions";

@Injectable()
export class PipelinesEffects {
    @Effect() editPipeline$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        switchMap(action => {
            const regex = /\/pipeline\/.*/g;
            if (this.handleNavigation(regex, action as RouterNavigationAction)) {
                const id = this.getPipelineIdfromRouterAction(action as RouterNavigationAction);
                return of(new EditPipelineAction(id));
            }
            return of();
        })
    );

    @Effect() getEditPipelineConfig$: Observable<Action> = this.actions$.pipe(
        ofType(EDIT_PIPELINE),
        map(action => (action as EditPipelineAction).id),
        combineLatest(this.store.select(selectAppConfig)),
        switchMap(([pipelineId, config]) => {
            const pipelineUrl: string = config.getString('keyscore.frontier.base-url') + '/pipeline/configuration/';
            return this.http.get(pipelineUrl + pipelineId).pipe(
                map((data: PipelineConfiguration) => new EditPipelineSuccessAction(data)),
                catchError((cause: any) => of(new EditPipelineFailureAction(pipelineId, cause)))
            );
        })
    );


    @Effect() updatePipeline$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_PIPELINE),
        map(action => (action as UpdatePipelineAction).pipeline),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([pipeline, config]) => {
            const pipelineUrl: string = config.getString('keyscore.frontier.base-url') + '/pipeline/configuration';
            let pipelineConfig = toPipelineConfiguration(pipeline);
            return this.http.put(pipelineUrl, pipelineConfig, {
                headers: new HttpHeaders().set('Content-Type', 'application/json'),
                responseType: 'text'
            }).pipe(
                map(data => new UpdatePipelineSuccessAction(pipeline)),
                catchError((cause: any) => of(new UpdatePipelineFailureAction(cause, pipeline)))
            );
        })
    );

    @Effect() updatePipelineBlockly$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_PIPELINE_BLOCKLY),
        map(action => (action as UpdatePipelineWithBlocklyAction)),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([updateAction, config]) => {
            const pipelineUrl: string = config.getString('keyscore.frontier.base-url') + '/pipeline/configuration';
            return this.http.put(pipelineUrl, updateAction.pipelineConfiguration, {
                headers: new HttpHeaders().set('Content-Type', 'application/json'),
                responseType: 'text'
            }).pipe(
                map((data: any) => new UpdatePipelineSuccessAction(toInternalPipelineConfig(updateAction.pipelineConfiguration))),
                catchError((cause: any) => of(new UpdatePipelineFailureAction(cause, toInternalPipelineConfig(updateAction.pipelineConfiguration))))
            );
        })
    );

    @Effect() deletePipeline$: Observable<Action> = this.actions$.pipe(
        ofType(DELETE_PIPELINE),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([action, config]) => {
            const pipelineUrl: string = config.getString('keyscore.frontier.base-url') + '/pipeline/configuration/';
            const pipelineId: string = (action as DeletePipelineAction).id;
            return this.http.delete(pipelineUrl + pipelineId).pipe(
                map(data => new DeletePipelineSuccessAction(pipelineId)),
                catchError((cause: any) => of(new DeletePipelineFailureAction(cause, pipelineId)))
            )
        })
    );

    @Effect() loadFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_DESCRIPTORS),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([action, config]) =>
            this.http.get(config.getString('keyscore.frontier.base-url') + '/descriptors?language=' + this.translate.currentLang).pipe(
                map((data: FilterDescriptor[]) => new LoadFilterDescriptorsSuccessAction(data)),
                catchError(cause => of(new LoadFilterDescriptorsFailureAction(cause)))
            )
        )
    );

    constructor(private store: Store<AppState>, private actions$: Actions, private http: HttpClient, private translate: TranslateService) {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        return regEx.test(action.payload.event.url);

    }

    private getPipelineIdfromRouterAction(action: RouterNavigationAction) {
        return action.payload.routerState.root.firstChild.firstChild.url[1].path;
    }
}