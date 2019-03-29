import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Actions, Effect, ofType, ROOT_EFFECTS_INIT} from "@ngrx/effects";
import {Action, select, Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable, of} from "rxjs";
import {switchMap, take} from "rxjs/operators";
import {AppState} from "./app.component";
import {KeycloakConfig, KeycloakService} from "keycloak-angular";

export const CONFIG_LOADED = "[AppConfig] Loaded";
export const CONFIG_FAILURE = "[AppConfig] Failure";
export const LANGUAGE_INIT = "[AppConfig] LanguageInitialised";

export class AppConfigLoaded implements Action {
    public readonly type = CONFIG_LOADED;

    constructor(readonly payload: any) {
    }
}

export class AppConfigFailure implements Action {
    public readonly type = CONFIG_FAILURE;

    constructor(readonly cause: any) {
    }
}

export class LanguageInitialised implements Action {
    public readonly type = LANGUAGE_INIT;
}

export type AppConfigActions =
    | AppConfigLoaded
    | AppConfigFailure
    | LanguageInitialised;

@Injectable()
export class AppConfigEffects {

    @Effect() public initLanguage$: Observable<Action> = this.actions$.pipe(
        ofType(ROOT_EFFECTS_INIT),
        switchMap((action) => {
                this.translate.addLangs(["en", "de"]);
                this.translate.setDefaultLang("en");
                const browserLang = this.translate.getBrowserLang();
                this.translate.use(browserLang.match(/en|de/) ? browserLang : "en");
                return of(new LanguageInitialised());
            }
        )
    );

    constructor(private http: HttpClient,
                private actions$: Actions,
                private translate: TranslateService) {
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

    private readonly configuration: any;

    constructor(configuration: any) {
        this.configuration = configuration;
    }

    public getString(key: string): string {
        return this.resolveValue(key.split("."), this.configuration) as string;
    }

    public getBoolean(key: string): boolean {
        return this.resolveValue(key.split("."), this.configuration) as boolean;
    }

    public getObject<T>(key: string): T {
        return this.resolveValue(key.split("."), this.configuration) as T;
    }

    private resolveValue(keys: string[], config: any): any {
        if (keys.length === 1) {
            return config[keys[0]];
        } else {
            return this.resolveValue(keys, config[keys.shift()]);
        }
    }
}

@Injectable()
export class AppConfigLoader {
    constructor(private http: HttpClient, private store: Store<AppState>) {

    }

    public load(): Promise<any> {
        return new Promise((resolve, reject) => {

            this.http.get("application.conf").subscribe(
                (data) => {
                    this.store.dispatch(new AppConfigLoaded(data));
                    resolve();
                },
                (err) => {
                    this.store.dispatch(new AppConfigFailure(err));
                    resolve();

                }
            );
        });
    }
}

@Injectable()
export class KeycloakConfigLoader {
    constructor(private store: Store<AppState>) {
    }

    public isKeycloakEnabled(): Promise<boolean> {
        return new Promise<boolean>((resolve, reject) => {
            this.store.pipe(select(selectAppConfig), take(1)).subscribe(conf =>
                resolve(conf.getBoolean("keyscore.keycloak.enabled"))
            )
        })
    }

    public getKeycloakConfig(): Promise<KeycloakConfig> {
        return new Promise<KeycloakConfig>((resolve, reject) => {
            this.store.pipe(select(selectAppConfig), take(1)).subscribe(conf =>
                resolve(conf.getObject<KeycloakConfig>("keyscore.keycloak.config"))
            );
        })
    }
}

export function initializer(configLoader: AppConfigLoader, keycloakConfigLoader: KeycloakConfigLoader,
                            keycloak: KeycloakService): () => Promise<any> {
    return (): Promise<any> => {
        return new Promise(async (resolve, reject) => {
            try {
                await configLoader.load();
                const keycloakConf = await keycloakConfigLoader.getKeycloakConfig();
                const isKeycloakEnabled = await keycloakConfigLoader.isKeycloakEnabled();
                if(isKeycloakEnabled) {
                    await keycloak.init({
                        config: keycloakConf,
                        initOptions: {
                            onLoad: 'login-required',
                            checkLoginIframe: false
                        },
                        bearerExcludedUrls: []
                    });
                }
                resolve();
            } catch (error) {
                reject(error);
            }
        });
    }
}
