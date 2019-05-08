import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {Observable, of} from "rxjs/index";
import {HttpClient} from "@angular/common/http";
import {ROUTER_NAVIGATION,RouterNavigationAction} from "@ngrx/router-store";
import {mergeMap} from "rxjs/internal/operators";
import {catchError, map, switchMap} from "rxjs/operators";
import {
    GET_RESOURCE_STATE,
    GetResourceStateAction,
    GetResourceStateFailure,
    GetResourceStateSuccess,
    LOAD_ALL_BLUEPRINTS_SUCCESS,
    LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_SUCCESS,
    LoadAllBlueprintsActionFailure,
    LoadAllBlueprintsActionSuccess,
    LoadAllDescriptorsForBlueprintFailureAction,
    LoadAllDescriptorsForBlueprintSuccessAction,
    LoadConfigurationsFailureAction,
    LoadConfigurationsSuccessAction,
    ResolvedAllDescriptorsSuccessAction,
} from "./resources.actions";
import {
    Blueprint,
    Configuration,
    Descriptor,
    ResolvedFilterDescriptor,
    ResourceInstanceState
} from "keyscore-manager-models";
import {AppState} from "../app.component";
import {FilterControllerService} from "../services/rest-api/FilterController.service";
import {StringTMap} from "../common/object-maps";
import {DescriptorResolverService} from "../services/descriptor-resolver.service";
import {BlueprintService} from "../services/rest-api/BlueprintService";
import {ConfigurationService} from "../services/rest-api/ConfigurationService";
import {DescriptorService} from "../services/rest-api/DescriptorService";

@Injectable()
export class ResourcesEffects {
    @Effect()
    public initializing$: Observable<Action> = this.actions$.pipe(
        ofType(ROUTER_NAVIGATION),
        mergeMap((action) => {
            const regex = /\/resources/g;
            if (this.handleNavigation(regex, action as RouterNavigationAction)) {
                return this.blueprintService.loadAllBlueprints().pipe(
                    map((data: Blueprint[]) =>
                        new LoadAllBlueprintsActionSuccess(data)),
                    catchError((cause: any) => of(new LoadAllBlueprintsActionFailure(cause)))
                )
            }
            return of({type:'NOOP'});
        })
    );

    @Effect() public loadFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_ALL_BLUEPRINTS_SUCCESS),
        switchMap((_) =>
            this.descriptorService.getAllDescriptors().pipe(
                map((descriptorsMap: StringTMap<Descriptor>) => new LoadAllDescriptorsForBlueprintSuccessAction(Object.values(descriptorsMap))),
                catchError((cause) => of(new LoadAllDescriptorsForBlueprintFailureAction(cause)))
            )
        )
    );

    @Effect() public loadConfigurations$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_ALL_BLUEPRINTS_SUCCESS),
        switchMap((_) =>
            this.configurationService.getAllConfigurations().pipe(
                map((configurations: Configuration[]) => new LoadConfigurationsSuccessAction(configurations)),
                catchError((cause) => of(new LoadConfigurationsFailureAction(cause)))
            )
        )
    );

    @Effect() public resolveFilterDescriptors$: Observable<Action> = this.actions$.pipe(
        ofType(LOAD_ALL_DESCRIPTORS_FOR_BLUEPRINT_SUCCESS),
        map(action => (action as LoadAllDescriptorsForBlueprintSuccessAction).descriptors),
        map(descriptors => {
            let resolvedDescriptors: ResolvedFilterDescriptor[] = descriptors.map(descriptor =>
                this.descriptorResolver.resolveDescriptor(descriptor)
            );
            return new ResolvedAllDescriptorsSuccessAction(resolvedDescriptors);
        })
    );

    @Effect() public getStateOfResource$: Observable<Action> = this.actions$.pipe(
        ofType(GET_RESOURCE_STATE),
        map(action => (action as GetResourceStateAction).resourceId),
        mergeMap((resourceId) =>
            this.filterControllerService.getState(resourceId).pipe(
                map((instance: ResourceInstanceState) => new GetResourceStateSuccess(resourceId, instance)),
                catchError((cause) => of(new GetResourceStateFailure(cause)))
            )
        )
    );

    constructor(private store: Store<AppState>,
                private httpClient: HttpClient,
                private actions$: Actions,
                private filterService: FilterControllerService,
                private descriptorResolver: DescriptorResolverService,
                private configurationService: ConfigurationService,
                private blueprintService: BlueprintService,
                private descriptorService: DescriptorService,
                private filterControllerService: FilterControllerService) {
    }

    private handleNavigation(regEx: RegExp, action: RouterNavigationAction) {
        console.log("URL: ", action.payload.event.url);
        return regEx.test(action.payload.event.url);

    }
}