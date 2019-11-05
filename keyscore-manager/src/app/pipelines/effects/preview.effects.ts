import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, Store} from "@ngrx/store";
import {AppState} from "../../app.component";
import {Observable, of} from "rxjs";
import {catchError, map, mergeMap, retry} from "rxjs/operators";
import {
    EXTRACT_FROM_SELECTED_BLOCK,
    ExtractFromSelectedBlock,
    ExtractFromSelectedBlockFailure,
    ExtractFromSelectedBlockSuccess
} from "../actions/preview.actions";
import {FilterControllerService} from "@keyscore-manager-rest-api/src/main/FilterController.service";
import {Dataset} from "@/../modules/keyscore-manager-models/src/main/dataset/Dataset";

@Injectable()
export class PreviewEffects {

    @Effect()
    public extractDataFromFilter$: Observable<Action> = this.actions$.pipe(
        ofType(EXTRACT_FROM_SELECTED_BLOCK),
        map((action) => (action as ExtractFromSelectedBlock)),
        mergeMap((action) =>
            this.filterControllerService.extractDatasets(action.selectedBlockId, action.amount, action.where).pipe(
                retry(5),
                map((data: Dataset[]) => new ExtractFromSelectedBlockSuccess(data, action.selectedBlockId, action.where),
                    catchError((cause: any) => of(new ExtractFromSelectedBlockFailure(cause))))
            )
        )
    );

    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private filterControllerService: FilterControllerService) {
    }

}