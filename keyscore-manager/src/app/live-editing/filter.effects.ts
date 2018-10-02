import {Injectable} from "@angular/core";
import {AppState} from "../app.component";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Observable, of} from "rxjs/index";
import {ROUTER_NAVIGATION, RouterNavigationAction} from "@ngrx/router-store";
import {catchError, concatMap, map, mergeMap} from "rxjs/internal/operators";
import {Action, Store} from "@ngrx/store";
import {
    DRAIN_FILTER,
    DrainFilterAction,
    DrainFilterFailure,
    DrainFilterSuccess,
    LOAD_FILTER_BLUEPRINT_SUCCESS,
    LOAD_FILTER_CONFIGURATION,
    LOAD_FILTERSTATE,
    LoadFilterBlueprintFailure,
    LoadFilterBlueprintSuccess,
    LoadFilterConfigurationAction,
    LoadFilterConfigurationFailure,
    LoadFilterConfigurationSuccess,
    LoadFilterStateAction, LoadFilterStateSuccess,
    PAUSE_FILTER,
    PauseFilterAction,
    PauseFilterFailure,
    PauseFilterSuccess
} from "./filters.actions";
import {RestCallService} from "../services/rest-api/rest-call.service";
import {Configuration} from "../models/common/Configuration";
import {Blueprint} from "../models/blueprints/Blueprint";
import {ResourceInstanceState} from "../models/filter-model/ResourceInstanceState";


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
                    catchError((cause) => of(new LoadFilterConfigurationFailure(cause)))
                ));
        })
    );




    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private restCallService: RestCallService) {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        return regEx.test(action.payload.event.url);
    }

    private getFilterBlueprintId(action: RouterNavigationAction): string {
        const url = action.payload.event.url;
        return url.substring(url.lastIndexOf("/") + 1, url.length)
    }
}