import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs/Rx';
import {Actions, Effect, ofType, ROOT_EFFECTS_INIT} from "@ngrx/effects";
import {catchError, map, mergeMap} from "rxjs/operators";
import {of} from "rxjs/observable/of";
import {Action} from "@ngrx/store";
import {AppState} from "./app.component";

export const CONFIG_LOADED = '[AppConfig] Loaded';
export const CONFIG_FAILURE = '[AppConfig] Failure';

export class Loaded implements Action {
    readonly type: '[AppConfig] Loaded';

    constructor(public payload: Object) {
    }
}

export class Failure implements Action {
    readonly type = '[AppConfig] Failure';
}

export type AppConfigActions =
    | Loaded
    | Failure;

@Injectable()
export class AppConfigEffects {

    @Effect() init$: Observable<Action> = this.actions$.pipe(
        ofType(ROOT_EFFECTS_INIT),
        mergeMap(action =>
            this.http.get('application.conf').pipe(
                map(data => ({type: CONFIG_LOADED, payload: data})),
                catchError(() => of({type: CONFIG_FAILURE}))
            )

        )
    );

    constructor(private http: HttpClient,
                private actions$: Actions) {
    }
}

export function AppConfigReducer(state: AppConfig, action: AppConfigActions): AppConfig {
    switch (action.type) {
        case CONFIG_LOADED:
            return new AppConfig(action.payload);
        case CONFIG_FAILURE:
        default:
            return state;
    }
}

export const selectAppConfig = (state: AppState) => state.config;

export class AppConfig {

    private readonly configuration: Object;

    constructor(configuration: Object) {
        this.configuration = configuration;
    }

    public getString(key: string): string {
        return <string>this.resolveValue(key.split('.'), this.configuration)
    }

    private resolveValue(keys: Array<string>, config: Object): any {
        if (keys.length == 1) {
            return config[keys[0]]
        }
        else {
            return this.resolveValue(keys, config[keys.shift()])
        }
    }
}