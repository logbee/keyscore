import {Component, Input} from "@angular/core";
import {Observable} from "rxjs/index";
import {Store} from "@ngrx/store";
import {selectResultAvailable} from "../../filter.reducer";

@Component({
    selector: "filter-result",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold" style="color: black;">
                {{'FILTERLIVEEDITINGCOMPONENT.RESULT' | translate}}
            </div>
            <div class="card-body">
                <div class="form-group" align="center" *ngIf="(loading$ | async); else loading">
                    <h4>{{'FILTERLIVEEDITINGCOMPONENT.NORESULT' | translate}}</h4>
                </div>
                <button class="mt-3 btn float-right primary btn-success"> {{'GENERAL.SAVE' | translate}}</button>
            </div>
        </div>
        <ng-template #loading>
            <div class="row">
                <div class="col-sm-12" align="center">
                    <loading></loading>
                </div>
            </div>
        </ng-template>
    `
})

export class FilterResultComponent {
    private loading$: Observable<boolean>;

    constructor(private store: Store<any>) {
        this.loading$ = this.store.select(selectResultAvailable);
    }
}
