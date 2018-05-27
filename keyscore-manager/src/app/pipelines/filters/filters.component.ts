import { Component } from '@angular/core';

@Component({
    selector: 'keyscore-filters',
    template: `
        <div class="row justify-content-center">
            <div class="col-10">
                <div class="card">
                    <div class="card-header">
                        <div class="d-flex justify-content-between">
                            <h4>{{'FILTERSCOMPONENT.FILTERS' | translate}}</h4>
                            <button type="button" class="btn btn-success" routerLink="/filter/new">{{'GENERAL.NEW' | translate}}</button>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="list-group">
                            <div class="list-group-item">
                                <div class="d-flex justify-content-between">
                                    <span class="font-weight-bold">Filter 1</span>
                                    <div>
                                        <button type="button" class="btn btn-primary" routerLink="/filter/details">{{'GENERAL.EDIT' | translate}}</button>
                                        <button type="button" class="btn btn-danger">{{'GENERAL.DELETE' | translate}}</button>
                                    </div>
                                </div>
                            </div>
                            <div class="list-group-item">
                                <div class="d-flex justify-content-between">
                                    <span class="font-weight-bold">Filter 2</span>
                                    <div>
                                        <button type="button" class="btn btn-primary" routerLink="/filter/details">{{'GENERAL.EDIT' | translate}}</button>
                                        <button type="button" class="btn btn-danger">{{'GENERAL.DELETE' | translate}}</button>
                                    </div>
                                </div>
                            </div>
                            <div class="list-group-item">
                                <div class="d-flex justify-content-between">
                                    <span class="font-weight-bold">Filter 3</span>
                                    <div>
                                        <button type="button" class="btn btn-primary" routerLink="/filter/details">{{'GENERAL.EDIT' | translate}}</button>
                                        <button type="button" class="btn btn-danger">{{'GENERAL.DELETE' | translate}}</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})

export class FiltersComponent {

}
