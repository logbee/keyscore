import {Component} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs/index";
import {FilterConfiguration, FilterState, getLiveEditingFilter} from "../../pipelines.model";
import {AppState} from "../../../app.component";
import {isSpinnerShowing} from "../../../common/loading/loading.reducer";

@Component({
    selector: "live-editing",
    template: `
        <div *ngIf="!(loading$ | async); else loading">
            <div class="col-12" *ngIf="!errorHandling">
                <div class="card">
                    <div class="card-header alert-info">
                        {{'FILTERLIVEEDITINGCOMPONENT.TITLE' | translate}}
                    </div>
                    <div class="card-body badge-light">

                        <filter-description [currentFilter]="filter$ | async"></filter-description>

                        <example-message></example-message>

                        <pattern (apply)="applyConfiguration($event)"></pattern>

                        <filter-result></filter-result>

                        <button class="mt-3 btn float-right primary btn-success"> {{'GENERAL.SAVE' | translate}}
                        </button>
                    </div>
                </div>
            </div>
            <div class="col-12">
                <error-component *ngIf="errorHandling" [httpError]="httpError"
                                 [message]="errorMessage"></error-component>
            </div>
        </div>
        <ng-template #loading>
        <div class="col-12">
            <loading-full-view></loading-full-view>
        </div>
        </ng-template>
    `
})

export class LiveEditingComponent {

    private filter$: Observable<FilterConfiguration>;
    private errorHandling: boolean = false;
    private errorMessage: string;
    private httpError: string;
    private loading$: Observable<boolean>;

    constructor(private store: Store<FilterState>, private translate: TranslateService) {
        this.loading$ = this.store.select(isSpinnerShowing);
        this.filter$ = this.store.select(getLiveEditingFilter);
        // this.filter$.subscribe((filter) => {
                // this.errorHandling = true;
                // this.translate.get("FILTERLIVEEDITINGCOMPONENT.NOTFOUND").subscribe(
                //     (translation) => this.errorMessage = translation);
                // this.httpError = "404";
        // });
    }

    public applyConfiguration(regex: string) {
        console.log("applyConfiguration:" + regex);
    }
}
