import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Observable} from "rxjs/Observable";
import {catchError, combineLatest, map, mergeMap} from "rxjs/operators";
import {of} from "rxjs/observable/of";
import {Action, Store} from "@ngrx/store";
import {AppConfig, selectAppConfig} from "../app.config";
import {AppState} from "../app.component";


export interface FilterDescriptor {
    name: string;
    displayName: string;
    description: string;
    parameters: ParameterDescriptor[];
}

export interface ParameterDescriptor {
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
}

export class FilterBlueprint {
    constructor(public name: string,
                public displayName: string,
                public description: string) {
    }
}

export const LOAD_FILTER_DESCRIPTORS = '[FilterDescriptor] LOAD';
export const LOAD_FILTER_DESCRIPTORS_SUCCESS = '[FilterDescriptor] LOAD_SUCCESS';
export const LOAD_FILTER_DESCRIPTORS_FAILURE = '[FilterDescriptor] LOAD_FAILURE';


export class LoadFilterDescriptorsSuccessAction implements Action {
    readonly type = '[FilterDescriptor] LOAD_SUCCESS';

    constructor(readonly descriptors: FilterDescriptor[]) {
    }
}

export class LoadFilterDescriptorsFailureAction implements Action {
    readonly type = '[FilterDescriptor] LOAD_FAILURE';

    constructor(readonly cause: any) {
    }
}

export type FilterDescriptorAction =
    | LoadFilterDescriptorsSuccessAction
    | LoadFilterDescriptorsFailureAction

@Injectable()
export class FilterService {

    private readonly appConfig$: Observable<AppConfig>;

    @Effect() loadFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_DESCRIPTORS),
        combineLatest(this.store.select(selectAppConfig)),
        mergeMap(([action, config]) =>
            this.http.get(config.getString('keyscore.frontier.base-url') + '/descriptors').pipe(
                map((data: FilterDescriptor[]) => new LoadFilterDescriptorsSuccessAction(data)),
                catchError(cause => of(new LoadFilterDescriptorsFailureAction(cause)))
            )
        )
    );

    constructor(private store: Store<AppState>,
                private http: HttpClient,
                private actions$: Actions) {

        this.appConfig$ = store.select(selectAppConfig);
    }
}