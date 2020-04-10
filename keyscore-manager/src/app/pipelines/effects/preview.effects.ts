import {Injectable} from "@angular/core";
import {Actions, Effect, ofType} from "@ngrx/effects";
import {Action, select, Store} from "@ngrx/store";
import {AppState} from "../../app.component";
import {BehaviorSubject, EMPTY, Observable, of, Subject} from "rxjs";
import {
    catchError,
    delay,
    filter,
    map,
    mergeMap,
    retry,
    retryWhen,
    switchMap,
    takeUntil,
    takeWhile,
    tap
} from "rxjs/operators";
import {
    DATA_PREVIEW_TOGGLE_VIEW, DataPreviewToggleView,
    EXTRACT_FROM_SELECTED_BLOCK,
    ExtractFromSelectedBlock,
    ExtractFromSelectedBlockFailure,
    ExtractFromSelectedBlockSuccess
} from "../actions/preview.actions";
import {FilterControllerService} from "@keyscore-manager-rest-api/src/main/FilterController.service";
import {Dataset} from "@/../modules/keyscore-manager-models/src/main/dataset/Dataset";
import {isPreviewVisible} from "@/app/pipelines/reducers/module";

@Injectable()
export class PreviewEffects {

    @Effect()
    public extractDataFromFilterAfter$: Observable<Action> = this.actions$.pipe(
        ofType(EXTRACT_FROM_SELECTED_BLOCK),
        map((action) => (action as ExtractFromSelectedBlock)),
        filter(action => action.where === 'after'),
        tap(() => this.errorCounterAfter = 1),
        switchMap((action) =>
            this.filterControllerService.extractDatasets(action.selectedBlockId, action.amount, action.where).pipe(
                retryWhen(errors => errors.pipe(
                    tap(() => {
                        this.store.dispatch(new ExtractFromSelectedBlockFailure(null, action.where));
                        this.errorCounterAfter = Math.min(this.errorCounterAfter + 1, 10);
                    }),
                    delay(1000 * this.errorCounterAfter),
                    takeWhile(() => this._dataPreviewVisibility))
                ),
                map((data: Dataset[]) => new ExtractFromSelectedBlockSuccess(data, action.selectedBlockId, action.where),
                    catchError((cause: any) => of(new ExtractFromSelectedBlockFailure(cause, action.where))))
            )
        )
    );

    @Effect()
    public extractDataFromFilterBefore$: Observable<Action> = this.actions$.pipe(
        ofType(EXTRACT_FROM_SELECTED_BLOCK),
        map((action) => (action as ExtractFromSelectedBlock)),
        filter(action => action.where === 'before'),
        tap(() => this.errorCounterBefore = 1),
        switchMap((action) =>
            this.filterControllerService.extractDatasets(action.selectedBlockId, action.amount, action.where).pipe(
                retryWhen(errors => errors.pipe(
                    tap(() => {
                        this.store.dispatch(new ExtractFromSelectedBlockFailure(null, action.where));
                        this.errorCounterBefore = Math.min(this.errorCounterBefore + 1, 10);
                    }),
                    delay(1000 * this.errorCounterBefore),
                    takeWhile(() => this._dataPreviewVisibility))
                ),
                map((data: Dataset[]) => new ExtractFromSelectedBlockSuccess(data, action.selectedBlockId, action.where),
                    catchError((cause: any) => of(new ExtractFromSelectedBlockFailure(cause, action.where))))
            )
        )
    );

    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private filterControllerService: FilterControllerService) {

        this.store.pipe(select(isPreviewVisible)).subscribe((val: boolean) => this._dataPreviewVisibility = val);
    }

    set errorCounterAfter(val: number) {
        this._errorCounterAfter = val;
    };

    get errorCounterAfter(): number {
        return this._errorCounterAfter;
    }

    private _errorCounterAfter: number = 1;

    set errorCounterBefore(val: number) {
        this._errorCounterBefore = val;
    };

    get errorCounterBefore(): number {
        return this._errorCounterBefore;
    }

    private _errorCounterBefore: number = 1;
    private _dataPreviewVisibility: boolean = false;

}
