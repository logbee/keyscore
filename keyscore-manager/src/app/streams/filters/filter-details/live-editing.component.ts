import {Component} from "@angular/core";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs/index";
import {FilterDescriptor, FilterState, getCurrentFilter} from "../../streams.model";

@Component({
    selector: 'live-editing',
    template: `
        <div class="col-12">
            <div class="card">
                <div class="card-header alert-info">
                    {{'FILTERLIVEEDITINGCOMPONENT.TITLE' | translate}}
                </div>
                <div class="card-body badge-light">
                    <div class="card">
                        <div class="card-header alert-light font-weight-bold">
                            {{'FILTERLIVEEDITINGCOMPONENT.FILTERDESCRIPTION_TITLE' | translate}}
                        </div>
                        <div class="card-body">
                            <table class="table table-condensed">
                                <thead>
                                <tr>
                                    <th> {{'FILTERLIVEEDITINGCOMPONENT.NAME' | translate}}</th>
                                    <th> {{'FILTERLIVEEDITINGCOMPONENT.DESCRIPTION' | translate}}</th>
                                    <th> {{'FILTERLIVEEDITINGCOMPONENT.CATEGORY' | translate}}</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                        <td>{{(currentFilter$ | async)?.displayName}}</td>
                                        <td>{{(currentFilter$ | async)?.description}}</td>
                                        <td>{{(currentFilter$ | async)?.category}}</td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="card mt-3">
                        <div class="card-header alert-light font-weight-bold">
                            {{'FILTERLIVEEDITINGCOMPONENT.EXAMPLE_MESSAGE' | translate}}
                        </div>
                        <div class="card-body">
                            <div class="form-group">
                                <textarea placeholder="{{'FILTERLIVEEDITINGCOMPONENT.MESSAGE_PLACEHOLDER' | translate}}"
                                          class="form-control" rows="5"></textarea>
                            </div>

                        </div>
                    </div>
                    <div class="card mt-3">
                        <div class="card-header alert-light font-weight-bold">
                            {{'FILTERLIVEEDITINGCOMPONENT.REGEXPATTERN' | translate}}
                        </div>
                        <div class="card-body">
                            <div class="form-group">
                                <textarea placeholder="{{'FILTERLIVEEDITINGCOMPONENT.REGEX_PLACEHOLDER' | translate}}"
                                          class="form-control" rows="1"></textarea>
                            </div>
                            <button class="float-right primary btn-info"> {{'GENERAL.APPLY' | translate}}</button>
                        </div>
                    </div>

                    <div class="card mt-3">
                        <div class="card-header alert-light font-weight-bold">
                            {{'FILTERLIVEEDITINGCOMPONENT.RESULT' | translate}}
                        </div>
                        <div class="card-body">
                            <div class="form-group">
                                <table class="table table-condensed">
                                    <thead>
                                    <tr>
                                        <th> {{'FILTERLIVEEDITINGCOMPONENT.NUMBER' | translate}}</th>
                                        <th> {{'FILTERLIVEEDITINGCOMPONENT.NAME' | translate}}</th>
                                        <th> {{'FILTERLIVEEDITINGCOMPONENT.VALUE' | translate}}</th>
                                        <th> {{'FILTERLIVEEDITINGCOMPONENT.TYPE' | translate}}</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td>1</td>
                                        <td>aggregatetField</td>
                                        <td>2.35</td>
                                        <td>Integer</td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <button class="mt-3 float-right primary btn-success"> {{'GENERAL.SAVE' | translate}}</button>
                </div>
            </div>
        </div>
    `
})

export class LiveEditingComponent {

    private currentFilter$: Observable<FilterDescriptor>;

    constructor(private store: Store<FilterState>) {
        this.currentFilter$ = this.store.select(getCurrentFilter);
    }
}