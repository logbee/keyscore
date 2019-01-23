import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {AppState} from "../../app.component";
import {FilterControllerService} from "../../services/rest-api/FilterController.service";
import {ConfigurationService} from "../../services/rest-api/ConfigurationService";
import {DescriptorService} from "../../services/rest-api/DescriptorService";
import {DescriptorResolverService} from "../../services/descriptor-resolver.service";
import {PipelineService} from "../../services/rest-api/PipelineService";
import {BlueprintService} from "../../services/rest-api/BlueprintService";
import {forkJoin, Observable, of} from "rxjs";
import {catchError, concatMap, map, mergeMap, switchMap} from "rxjs/operators";
import {
    EXTRACT_FROM_SELECTED_BLOCK,
    ExtractFromSelectedBlock, ExtractFromSelectedBlockFailure,
    ExtractFromSelectedBlockSuccess
} from "../actions/preview.actions";
import {Dataset} from "../../models/dataset/Dataset";

@Injectable()
export class PreviewEffects {

    @Effect()
    public extractDataFromFilter$: Observable<Action> = this.actions$.pipe(
        ofType(EXTRACT_FROM_SELECTED_BLOCK),
        map((action) => (action as ExtractFromSelectedBlock)),
        mergeMap((action) => {
            return this.filterControllerService.extractDatasets(action.selectedBlockId, action.amount, action.where).pipe(
                map((data: Dataset[]) => new ExtractFromSelectedBlockSuccess(data, action.selectedBlockId, action.where),
                catchError((cause: any) => of(new ExtractFromSelectedBlockFailure(cause)))));
        }),
    );
    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private filterControllerService: FilterControllerService,
                private configurationService: ConfigurationService,
                private descriptorService: DescriptorService,
                private descriptorResolver: DescriptorResolverService,
                private pipelineService: PipelineService,
                private blueprintService: BlueprintService) {
    }

}