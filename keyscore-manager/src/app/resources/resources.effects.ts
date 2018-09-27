import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {Observable, of} from "rxjs/index";
import {HttpClient} from "@angular/common/http";
import {ROUTER_NAVIGATION} from "@ngrx/router-store";
import {mergeMap} from "rxjs/internal/operators";
import {RouterNavigationAction} from "@ngrx/router-store/src/router_store_module";
import {catchError, map, switchMap} from "rxjs/operators";
import {LoadAllBlueprintsActionsFailure, LoadAllBlueprintsActionsSuccess} from "./resources.actions";
import {Blueprint} from "../models/blueprints/Blueprint";
import {AppState} from "../app.component";
import {FilterService} from "../services/rest-api/filter.service";
import {
    LOAD_FILTER_DESCRIPTORS, LOAD_FILTER_DESCRIPTORS_SUCCESS, LoadFilterDescriptorsAction,
    LoadFilterDescriptorsFailureAction, LoadFilterDescriptorsSuccessAction,
    ResolveFilterDescriptorSuccessAction
} from "../pipelines/pipelines.actions";
import {Descriptor} from "../models/descriptors/Descriptor";
import {ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {StringTMap} from "../common/object-maps";
import {DescriptorResolverService} from "../services/descriptor-resolver.service";
import {RestCallService} from "../services/rest-api/rest-call.service";

@Injectable()
export class ResourcesEffects {
    @Effect()
    public initializing$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
            const regex = /\/resources/g;
            if (this.handleNavigation(regex, action as RouterNavigationAction)) {
                return this.filterService.loadAllBlueprints().pipe(
                    map((data: Blueprint[]) =>
                        new LoadAllBlueprintsActionsSuccess(data)),
                    catchError((cause: any) => of(new LoadAllBlueprintsActionsFailure(cause)))
                )
            }
            return of();
        })
    );

    @Effect() public loadFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_DESCRIPTORS),
        switchMap((_) =>
            this.restCallService.getAllDescriptors().pipe(
                map((descriptorsMap: StringTMap<Descriptor>) => new LoadFilterDescriptorsSuccessAction(Object.values(descriptorsMap))),
                catchError((cause) => of(new LoadFilterDescriptorsFailureAction(cause)))
            )
        )
    );

    @Effect() public resolveFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_FILTER_DESCRIPTORS_SUCCESS),
        map(action => (action as LoadFilterDescriptorsSuccessAction).descriptors),
        map(descriptors => {
            let resolvedDescriptors: ResolvedFilterDescriptor[] = descriptors.map(descriptor =>
                this.descriptorResolver.resolveDescriptor(descriptor)
            );
            return new ResolveFilterDescriptorSuccessAction(resolvedDescriptors);
        })
    );

    constructor(private store: Store<AppState>,
                private httpClient: HttpClient,
                private actions$: Actions,
                private filterService: FilterService,
                private descriptorResolver: DescriptorResolverService,
                private restCallService: RestCallService) {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        console.log("URL: ", action.payload.event.url);
        return regEx.test(action.payload.event.url);

    }
}