import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {Action, Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {combineLatest, Observable, of} from "rxjs/index";
import {catchError, concatMap, map, mergeMap, switchMap, withLatestFrom} from "rxjs/internal/operators";
import {AppState} from "../app.component";
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
    INITIALIZE_LIVE_EDITING_DATA,
    InitializeLiveEditingDataAction,
    INSERT_DATASETS_SUCCESS,
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
    RECONFIGURE_FILTER_SUCCESS,
    ReconfigureFilterFailure,
    ReconfigureFilterSuccess
} from "./filters.actions";
import {selectAppConfig} from "../app.config";
import {FilterConfiguration} from "../models/filter-model/FilterConfiguration";
import {FilterInstanceState} from "../models/filter-model/FilterInstanceState";
import {Dataset} from "../models/filter-model/dataset/Dataset";
import {Record} from "../models/filter-model/dataset/Record";
import {
    selectExtractedDatasets,
    selectFilterId,
    selectLiveEditingFilter,
    selectUpdateConfigurationFlag
} from "./filter.reducer";
import {Field} from "../models/filter-model/dataset/Field";

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
            new LoadLiveEditingFilterAction(payload.filterId, 10)]
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
        ofType(RECONFIGURE_FILTER_SUCCESS),
        withLatestFrom(
            this.store.select(selectAppConfig),
            this.store.select(selectFilterId),
            this.store.select(selectExtractedDatasets)),
        switchMap(([_, config, filterId, datasets]) => {
            return this.http.put(config.getString("keyscore.frontier.base-url") +
                "/filter/" + filterId + "/insert?where=before" , this.convertDatasetsToBackend(datasets), {
                headers: new HttpHeaders().set("Content-Type", "application/json"),
                responseType: "json"
            }).pipe(
                map((state: FilterInstanceState) => new InsertDatasetsSuccess(state)),
                catchError((cause: any) => of(new InsertDatasetsFailure(cause)))
            );
        }),
    );

    @Effect()
    public fireExtractDatasetsWhenInsertDatasetsSuccessAction: Observable<Action> = this.actions$.pipe(
        ofType(INSERT_DATASETS_SUCCESS),
        withLatestFrom(this.store.select(selectFilterId), this.store.select(selectExtractedDatasets)),
        switchMap(([_, filterId, datasets] ) => of(new ExtractDatasetsAction(filterId, datasets.length)))
    );

    @Effect()
    public fireExtractDatasetsWhenLoadLiveEditEditingFilterSuccesAction: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_LIVE_EDITING_FILTER_SUCCESS),
        map((action) => (action as LoadLiveEditingFilterAction)),
        withLatestFrom(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.get(appconfig.getString("keyscore.frontier.base-url") +
                "/filter/" + action.filterId + "/extract?value=10&where=before").pipe(
                map((content) => this.convertDatasetsFromBackend(content as Object[])),
                map((datasets: Dataset[]) => new ExtractDatasetsInitialSuccess(datasets)),
                catchError((cause: any) => of(new ExtractDatasetsFailure(cause)))
            );
        }),
    );

    @Effect()
    public extractDatasets: Observable<Action> = this.actions$.pipe(
        ofType(EXTRACT_DATASETS),
        map((action) => (action as ExtractDatasetsAction)),
        withLatestFrom(this.store.select(selectAppConfig)),
        switchMap(([action, appconfig]) => {
            return this.http.get(appconfig.getString("keyscore.frontier.base-url") +
                "/filter/" + action.filterId + "/extract?value=" + action.amount + "&where=after").pipe(
                map((content) => this.convertDatasetsFromBackend(content as Object[])),
                map((datasets: Dataset[]) => new ExtractDatasetsResultSuccess(datasets)),
                catchError((cause: any) => of(new ExtractDatasetsFailure(cause)))
            );
        }),
    );

    @Effect()
    public updateConfiguration: Observable<Action> = combineLatest(this.store.select(selectLiveEditingFilter),
        this.store.select(selectAppConfig), this.store.select(selectUpdateConfigurationFlag)).pipe(
        switchMap(([filterConfiguration, appconfig, triggerCall]) => {
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
                return of();
            }
        })
    );

    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private http: HttpClient,
                private translate: TranslateService) {
    }

    private convertDatasetsFromBackend(datasets: Object[]) {
        const recordsExtractionValue: string = "records";
        const idExtractionValue: string = "id";
        const metaDataExtractionValue: string = "metaData";
        return datasets.map((dataset) => {
            const records: Record[] = dataset[recordsExtractionValue].map((record) => {
                return { id: record[idExtractionValue], payload: Object.values(record.payload)};
            });
            return {metaData: dataset[metaDataExtractionValue], records} as Dataset;
        });
    }

    private convertDatasetsToBackend(datasets: Dataset[]) {
        return datasets.map((dataset) => {
            const records = dataset.records.map((record: Record) => {

                const newRecord = {id: record.id, payload: {}};

                record.payload.forEach( (field: Field) => {
                    newRecord.payload[field.name] = field;
                });

                return newRecord;
            });
            console.log("=======>" + JSON.stringify(records));
            return {metaData: dataset.metaData, records};
        });

    }
}
