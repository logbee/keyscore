import {Injectable} from "@angular/core";
import {Actions} from "@ngrx/effects";
import {Store} from "@ngrx/store";
import {AppState} from "../../app.component";

@Injectable()
export class DataPreviewEffects {

    constructor(private store: Store<AppState>, private actions$: Actions) {

    }

}