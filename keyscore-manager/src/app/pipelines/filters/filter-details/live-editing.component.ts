import {Component} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs/index";
import {mergeMap} from "rxjs/internal/operators";
import {FilterConfiguration, FilterState, getFilterById, getFilterId} from "../../pipelines.model";

@Component({
    selector: "live-editing",
    template: `
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

                    <button class="mt-3 btn float-right primary btn-success"> {{'GENERAL.SAVE' | translate}}</button>
                </div>
            </div>
        </div>

        <error-component *ngIf="errorHandling" [httpError]="httpError" [message]="errorMessage"></error-component>
    `
})

export class LiveEditingComponent {

    private filter$: Observable<FilterConfiguration>;
    private errorHandling: boolean = false;
    private errorMessage: string;
    private httpError: string;

    constructor(private store: Store<FilterState>, private translate: TranslateService) {
        this.filter$ = this.store.select(getFilterId).pipe(mergeMap((id) => this.store.select(getFilterById(id))));

        this.filter$.subscribe((filter) => {
            if (typeof(filter) === "undefined") {
                this.errorHandling = true;
                this.translate.get("FILTERLIVEEDITINGCOMPONENT.NOTFOUND").subscribe(
                    (translation) => this.errorMessage = translation);
                this.httpError = "404";
            }
        });
    }

    public applyConfiguration(regex: string) {
        console.log("applyConfiguration:" + regex);
    }
}
