import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {Action, State, Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable, of, combineLatest} from "rxjs/index";
import {catchError, concatMap, map, mergeMap, switchMap, withLatestFrom} from "rxjs/internal/operators";
import {AppState} from "../app.component";
import {
    DRAIN_FILTER,
    DrainFilterAction,
    DrainFilterFailure,
    DrainFilterSuccess,
    ExtractDatasetsFailure,
    ExtractDatasetsSuccess,
    INITIALIZE_LIVE_EDITING_DATA,
    InitializeLiveEditingDataAction,
    INSERT_DATASETS,
    InsertDatasetsAction,
    InsertDatasetsFailure,
    InsertDatasetsSuccess,
    LOAD_FILTERSTATE,
    LOAD_LIVE_EDITING_FILTER,
    LOAD_LIVE_EDITING_FILTER_SUCCESS,
    LoadFilterStateAction,
    LoadFilterStateFailure,
    LoadFilterStateSuccess,
    LoadLiveEditingFilterAction,
    LoadLiveEditingFilterFailure,
    LoadLiveEditingFilterSuccess,
    PAUSE_FILTER,
    PauseFilterAction,
    PauseFilterFailure,
    PauseFilterSuccess,
    RECONFIGURE_FILTER_ACTION,
    ReconfigureFilterAction,
    ReconfigureFilterFailure,
    ReconfigureFilterSuccess
} from "./filters.actions";
import {selectAppConfig} from "../app.config";
import {FilterConfiguration} from "../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../models/filter-model/FilterInstanceState";
import {Dataset} from "../models/filter-model/dataset/Dataset";
import {selectLiveEditingFilter, selectUpdateConfigurationFlag} from "./filter.reducer";

@Injectable()
export class FilterEffects {
    @Effect()
    public initalizeLiveEditingData$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
                const navigationAction = action as RouterNavigationAction;
                const url = navigationAction.payload.event.url;
                const filterId = url.substring(url.lastIndexOf("/") + 1, url.length);
                const filterIdRegex =
                    /\/filter\/.*/g;
                if (filterIdRegex.test(url)) {
                    return of(new InitializeLiveEditingDataAction(filterId));
                } else {
                    return of();
                }
            }
        )
    );
    @Effect()
    public navigateToLiveEditing$: Observable<Action> = this.actions$.pipe(
        ofType(INITIALIZE_LIVE_EDITING_DATA),
        map((action) => (action as InitializeLiveEditingDataAction)),
        concatMap((payload) => [
            new PauseFilterAction(payload.filterId, true),
            new DrainFilterAction(payload.filterId, true),
            new LoadLiveEditingFilterAction(payload.filterId)]
        )
    );
    @Effect()
    public loadFilterConfiguration$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_LIVE_EDITING_FILTER),
        map((action) => (action as LoadLiveEditingFilterAction)),
        withLatestFrom(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.get(appconfig.getString("keyscore.frontier.base-url") +
                "/filter/" + action.filterId + "/config").pipe(
                map((data: FilterConfiguration) => new LoadLiveEditingFilterSuccess(data, action.filterId)),
                catchError((cause: any) => of(new LoadLiveEditingFilterFailure(cause)))
            );
        })
    );
    @Effect()
    public loadFilterState$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTERSTATE),
        map((action) => (action as LoadFilterStateAction)),
        withLatestFrom(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.get(appconfig.getString("keyscore.frontier.base-url") +
                "/filter/" + action.filterId + "/state").pipe(
                map((state: FilterInstanceState) => new LoadFilterStateSuccess(state)),
                catchError((cause: any) => of(new LoadFilterStateFailure(cause))));
        })
    );
    @Effect()
    public pauseFilter$: Observable<Action> = this.actions$.pipe(
        ofType(PAUSE_FILTER),
        map((action) => (action as PauseFilterAction)),
        withLatestFrom(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.post(appconfig.getString("keyscore.frontier.base-url") + "/filter/" +
                action.filterId + "/pause?value=" + action.pause, {}, {
                headers: new HttpHeaders().set("Content-Type", "application/json"),
                responseType: "json"
            }).pipe(
                map((state: FilterInstanceState) => new PauseFilterSuccess(state)),
                catchError((cause: any) => of(new PauseFilterFailure(cause)))
            );
        })
    );
    @Effect()
    public drainFilter$: Observable<Action> = this.actions$.pipe(
        ofType(DRAIN_FILTER),
        map((action) => (action as DrainFilterAction)),
        withLatestFrom(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.post(appconfig.getString("keyscore.frontier.base-url") + "/filter/" + action.filterId +
                "/drain?value=" + action.drain, {}, {
                headers: new HttpHeaders().set("Content-Type", "application/json"),
                responseType: "json"
            }).pipe(
                map((state: FilterInstanceState) => new DrainFilterSuccess(state)),
                catchError((cause: any) => of(new DrainFilterFailure(cause)))
            );
        })
    );
    @Effect()
    public insertDatasets: Observable<Action> = this.actions$.pipe(
        ofType(INSERT_DATASETS),
        map((action) => (action as InsertDatasetsAction)),
        withLatestFrom(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.put(appconfig.getString("keyscore.frontier.base-url") +
                "/filter/" + action.filterId + "/insert", action.datasets, {
                headers: new HttpHeaders().set("Content-Type", "application/json"),
                responseType: "json"
            }).pipe(
                map((state: FilterInstanceState) => new InsertDatasetsSuccess(state)),
                catchError((cause: any) => of(new InsertDatasetsFailure(cause)))
            );
        }),
    );
    @Effect()
    public extractDatasets: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_LIVE_EDITING_FILTER_SUCCESS),
        map((action) => (action as LoadLiveEditingFilterSuccess)),
        withLatestFrom(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.get(appconfig.getString("keyscore.frontier.base-url") +
                "/filter/" + action.filterId + "/extract?value=10").pipe(
                map((datasets: Dataset[]) => new ExtractDatasetsSuccess(datasets)),
                catchError((cause: any) => of(new ExtractDatasetsFailure(cause)))
            );
        }),
    );

    @Effect()
    public updateConfiguration: Observable<Action> = combineLatest(this.store.select(selectLiveEditingFilter),
        this.store.select(selectAppConfig), this.store.select(selectUpdateConfigurationFlag)).pipe(
        switchMap(([filterConfiguration, appconfig, triggerCall]) => {
            console.log(JSON.stringify(filterConfiguration), JSON.stringify(appconfig), triggerCall);
            if (triggerCall) {
            return this.http.put(appconfig.getString("keyscore.frontier.base-url") +
                    "/filter/" + filterConfiguration.id + "/config", filterConfiguration, {
                    headers: new HttpHeaders().set("Content-Type", "application/json"),
                    responseType: "json"
                }).pipe(
                    map((state: FilterInstanceState) => new ReconfigureFilterSuccess(state)),
                    catchError((cause: any) => of(new ReconfigureFilterFailure(cause)))
                );
            } else {
                console.log("not necessary to send configure call");
                return of();
            }
        })
    );

    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private http: HttpClient,
                private translate: TranslateService) {
    }

}
