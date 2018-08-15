import {Component, Input, OnInit} from "@angular/core";
import {Observable} from "rxjs/index";
import {Store} from "@ngrx/store";
import {selectResultAvailable} from "../../filter.reducer";
import {Dataset} from "../../../models/filter-model/dataset/Dataset";

@Component({
    selector: "filter-result",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold" style="color: black;">
                <div class="row">
                    <div class="col-sm-1">
                        {{'FILTERLIVEEDITINGCOMPONENT.RESULT' | translate}}
                    </div>
                    <div class="col-sm-2" *ngIf="(loading$ | async)">
                      <span (click)="goLeft()">
                            <img width="18em" src="/assets/images/chevron-left.svg"/>
                      </span>
                        {{count + 1}} / {{(resultDatasets$ | async)?.length}}
                        <span (click)="goRight()">
                            <img width="18em" src="/assets/images/chevron-right.svg"/>
                      </span>
                    </div>
                </div>
                <div class="col-sm-9"></div>
            </div>
            <div class="card-body">
                <div class="row" align="center" *ngIf="(loading$ | async); else loading">
                    <div class="col-sm-12">
                        <dataset-visualizer [dataset]="(resultDatasets$ | async)[count]">
                        </dataset-visualizer>
                    </div>
                </div>
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

export class FilterResultComponent implements OnInit {
    @Input() public resultDatasets$: Observable<Dataset[]>;
    private loading$: Observable<boolean>;
    private count: number;
    private numberOfDatasets: number;

    constructor(private store: Store<any>) {
        this.loading$ = this.store.select(selectResultAvailable);
    }

    public ngOnInit(): void {
        this.resultDatasets$.subscribe((datasets) => {
            this.numberOfDatasets = datasets.length;
        });
        this.count = 0;
    }

    private goLeft() {
        if (this.count == 0) {
            this.count = this.numberOfDatasets - 1;
        } else  {
            this.count -= 1;
        }
    }

    private goRight() {
        if (this.count == this.numberOfDatasets - 1) {
            this.count = 0;
        } else  {
            this.count += 1;
        }
    }

}
