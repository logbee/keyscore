import {Component, EventEmitter, Input, Output} from "@angular/core";
import {FilterModel} from "../streams.model";

@Component({
    selector: 'stream-filter',
    template: `
        <div class="card mb-1">
            <div class="card-title">
                <div class="row pl-2 pt-2 pr-2">
                    <div class="col-auto btn-group-vertical">
                        <button type="button" class="btn btn-light"
                                (click)="moveFilter(filter.id, i - 1)" [disabled]="i == 0">
                            <img width="12em" src="/assets/images/chevron-up.svg"/>
                        </button>
                        <button type="button" class="btn btn-light"
                                (click)="moveFilter(filter.id, i + 1)" [disabled]="i == filterCount - 1">
                            <img width="12em" src="/assets/images/chevron-down.svg"/>
                        </button>
                    </div>
                    <div class="col" style="margin-top: auto; margin-bottom: auto" *ngIf="!filter.editing">
                        <span class="font-weight-bold">{{filter.name}}</span><br>
                        <small>{{filter.description}}</small>
                    </div>
                    <div class="col" style="margin-top: auto; margin-bottom: auto" *ngIf="filter.editing">
                        <input id="filterName" class="form-control" placeholder="Name"
                               [(ngModel)]="filter.name"/>
                        <input id="filterDescription" class="form-control" placeholder="Description"
                               [(ngModel)]="filter.description"/>
                    </div>
                    <div class="col-2"></div>
                    <div class="col-auto">
                        <button type="button" class="btn btn-primary" *ngIf="!filter.editing"
                                (click)="editFilter()">Edit
                        </button>
                        <button type="button" class="btn btn-danger" *ngIf="filter.editing"
                                (click)="removeFilter()">Remove
                        </button>
                        <button type="button" class="btn btn-primary" *ngIf="filter.editing"
                                (click)="saveFilter()">Save
                        </button>
                    </div>
                </div>
            </div>
            <div class="card-body" *ngIf="filter.editing">
                <h6 class="font-weight-bold">Filter 1</h6>
            </div>
        </div>
    `
})
export class StreamFilterComponent {

    @Input() filter: FilterModel;

    @Output() update: EventEmitter<FilterModel> = new EventEmitter();

    i: number;

    constructor() {

    }

    removeFilter() {
    }

    moveFilter(id: number, position: number) {
    }

    editFilter() {
    }

    saveFilter() {
    }

    enableFilter() {
    }

    disableFilter() {
    }
}