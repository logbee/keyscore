import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Observable} from "rxjs/index";
import {Store} from "@ngrx/store";
import {selectcurrentDatasetCounter, selectExtractFinish, selectUpdateConfigurationFlag} from "../../filter.reducer";
import {Dataset} from "../../../models/filter-model/dataset/Dataset";

@Component({
    selector: "example-message",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold" style="color: black;">
                <div class="row">
                    <div class="col-sm-1">
                        {{'FILTERLIVEEDITINGCOMPONENT.EXAMPLE_MESSAGE' | translate}}
                    </div>
                    <div class="col-sm-2" *ngIf="!noDataAvailable">
                        <span class="mr-1" (click)="goLeft()">
                            <img width="18em" src="/assets/images/chevron-left.svg"/>
                        </span>
                        {{count + 1}}/ {{(extractedDatasets$ | async)?.length}}
                        <span (click)="goRight()">
                            <img width="18em" src="/assets/images/chevron-right.svg"/>
                        </span>
                    </div>
                    <div class="col-sm-9"></div>
                </div>
            </div>
            <div class="card-body">
                <div class="ml-3">
                    <div class="row" *ngIf="(!noDataAvailable); else noData">
                        <div class="col-sm-12 mb-2" *ngIf="(extractFinish$ | async); else loading">
                            <dataset-visualizer [dataset]="(extractedDatasets$ | async)[count]">
                            </dataset-visualizer>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <ng-template #loading>
            <div class="col-sm-10 mb-2" align="center">
                <loading></loading>
            </div>
        </ng-template>

        <ng-template #noData>
            <div class="col-sm-12" align="center">
                <h4>{{'FILTERLIVEEDITINGCOMPONENT.NODATA' | translate}}</h4>
            </div>
        </ng-template>
    `
})

export class ExampleMessageComponent implements OnInit {
    @Input() public extractedDatasets$: Observable<Dataset[]>;
    @Output() public currentDatasetCounter: EventEmitter<number> = new EventEmitter();
    private extractFinish$: Observable<boolean>;
    private count: number;
    private count$: Observable<number>;
    private isReady$: Observable<boolean>;
    private noDataAvailable: boolean = true;
    private numberOfDatasets: number;

    constructor(private store: Store<any>) {
        this.isReady$ = this.store.select(selectExtractFinish);
        this.extractFinish$ = this.store.select(selectExtractFinish);
        this.count$ = this.store.select(selectcurrentDatasetCounter)
    }

    public ngOnInit(): void {
        this.extractedDatasets$.subscribe((datasets) => {
            this.numberOfDatasets = datasets.length;
            this.noDataAvailable = datasets.length === 0;
        });
        this.count$.subscribe((count) => {
            this.count = count;
        })
    }

    private goLeft() {
        if (this.count == 0) {
            this.count = this.numberOfDatasets - 1;
            this.emitCounter(this.count);
        } else  {
            this.count -= 1;
            this.emitCounter(this.count)
        }
    }

    private goRight() {
        if (this.count == this.numberOfDatasets - 1) {
            this.count = 0;
            this.emitCounter(this.count);
        } else  {
            this.count += 1;
            this.emitCounter(this.count)
        }
    }

    private emitCounter(count: number) {
        this.currentDatasetCounter.emit(count)
    }
}
