import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs';
import {Actions, Effect, ofType, ROOT_EFFECTS_INIT} from "@ngrx/effects";
import {catchError, map, mergeMap, switchMap} from "rxjs/operators";
import {of} from "rxjs";
import {Action, Store} from "@ngrx/store";
import {AppState} from "./app.component";
import {TranslateService} from "@ngx-translate/core";

export const CONFIG_LOADED = '[AppConfig] Loaded';
export const CONFIG_FAILURE = '[AppConfig] Failure';
export const LANGUAGE_INIT = '[AppConfig] LanguageInitialised'

export class AppConfigLoaded implements Action {
    readonly type = CONFIG_LOADED;

    constructor(readonly payload: Object) {
    }
}

export class AppConfigFailure implements Action {
    readonly type = CONFIG_FAILURE;

    constructor(readonly cause: Object) {
    }
}

export class LanguageInitialised implements Action{
    readonly type = LANGUAGE_INIT
    constructor(){

    }
}

export type AppConfigActions =
    | AppConfigLoaded
    | AppConfigFailure
    | LanguageInitialised;

@Injectable()
export class AppConfigEffects {

    @Effect() initLanguage$: Observable<Action> = this.actions$.pipe(
        ofType(ROOT_EFFECTS_INIT),
        switchMap(action => {
                this.translate.addLangs(["en", "de"]);
                this.translate.setDefaultLang('en');
                let browserLang = this.translate.getBrowserLang();
                this.translate.use(browserLang.match(/en|de/) ? browserLang : 'en');
                return of(new LanguageInitialised())
            }
        )
    );

    constructor(private http: HttpClient,
                private actions$: Actions,private translate:TranslateService) {
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

    public getBoolean(key:string): boolean{
        return <boolean>this.resolveValue(key.split('.'),this.configuration)
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

@Injectable()
export class AppConfigLoader{
    constructor(private http:HttpClient, private store:Store<AppState>){

    }

    public load(){
        return new Promise((resolve,reject) => {
            this.http.get('application.conf').subscribe(
                data => {
                    this.store.dispatch(new AppConfigLoaded(data));
                    data
                    resolve();
                },
                err => {
                    this.store.dispatch(new AppConfigFailure(err));
                    resolve(true);

                }

            )
        })
    }
}