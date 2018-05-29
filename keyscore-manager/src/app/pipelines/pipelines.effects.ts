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
    DeletePipelineSuccessAction,
    EditPipelineAction,
    LOAD_FILTER_DESCRIPTORS,
    LoadFilterDescriptorsFailureAction,
    LoadFilterDescriptorsSuccessAction,
    UPDATE_PIPELINE,
    UpdatePipelineAction,
    UpdatePipelineFailureAction,
    UpdatePipelineSuccessAction
} from "./pipelines.actions";
import {HttpClient} from "@angular/common/http";
import {AppState} from "../app.component";
import {selectAppConfig} from "../app.config";
import {FilterDescriptor, getFilterDescriptors, PipelineConfiguration} from "./pipelines.model";
import {PipelineBuilderService} from "../services/pipelinebuilder.service";
import {TranslateService} from "@ngx-translate/core";

@Injectable()
export class PipelinesEffects {
    @Effect() editPipeline$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        switchMap(action => {
            const navigationAction = action as RouterNavigationAction;
            const url = navigationAction.payload.event.url;
            const regex = /\/pipeline\/.*/g;

            if (regex.test(url)) {
                const id = url.substring(url.indexOf('/pipeline/') + 10);
                return of(new EditPipelineAction(id));
            }
            return of({type: 'NOOP'});
        })
    );

    @Effect() updatePipeline$: Observable<Action> = this.actions$.pipe(
        ofType(UPDATE_PIPELINE),
        map(action => (action as UpdatePipelineAction).pipeline),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([pipeline, config]) => {
            console.log("test");
            const pipelineUrl: string = config.getString('keyscore.frontier.base-url') + '/pipeline/';
            var descriptorList: Array<FilterDescriptor> = [];
            this.store.select(getFilterDescriptors).subscribe(filterDescriptors => filterDescriptors.map(value =>  descriptorList.push(value)));
            const pipelineConfig: PipelineConfiguration = this.pipelineBuilder.toPipeline(pipeline, descriptorList);
            console.log('pipeline config: ' + JSON.stringify(pipelineConfig) + pipelineUrl);
            return this.http.put(pipelineUrl + pipeline.id, pipelineConfig).pipe(
                map(data => new UpdatePipelineSuccessAction(pipeline)),
                catchError((cause: any) => of(new UpdatePipelineFailureAction(pipeline)))
            );
        })
    );

    @Effect() deletePipeline$: Observable<Action> = this.actions$.pipe(
        ofType(DELETE_PIPELINE),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([action, config]) => {
            const pipelineUrl: string = config.getString('keyscore.frontier.base-url') + '/pipeline/';
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
                this.http.get(config.getString('keyscore.frontier.base-url') + '/descriptors?language='+this.translate.currentLang).pipe(
                    map((data: FilterDescriptor[]) => new LoadFilterDescriptorsSuccessAction(data)),
                    catchError(cause => of(new LoadFilterDescriptorsFailureAction(cause)))
                )

        )
    );

    constructor(private store: Store<AppState>, private actions$: Actions, private http: HttpClient, private pipelineBuilder: PipelineBuilderService, private translate: TranslateService) {
    }
}