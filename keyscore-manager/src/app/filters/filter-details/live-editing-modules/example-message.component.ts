import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Observable} from "rxjs/index";
import {Store} from "@ngrx/store";
import {getExtractFinish} from "../../filter.reducer";
import {Dataset} from "../../../models/filter-model/dataset/Dataset";
import {PipelineConfiguration} from "../../../models/pipeline-model/PipelineConfiguration";

@Component({
    selector: "example-message",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold" style="color: black;">
                {{'FILTERLIVEEDITINGCOMPONENT.EXAMPLE_MESSAGE' | translate}}
            </div>
            <div class="card-body">
                <div class="ml-3">
                    <div class="row" *ngIf="(noDataAvailable); else noData">
                        <div class="col-sm-1">
                            <span class="float-left chevron" (click)="goLeft()">
                                <img width="25em" src="/assets/images/chevron-left.svg"/>
                            </span>
                        </div>
                            <div class="col-sm-10 mb-2" *ngIf="(extractFinish$ | async); else loading">
                                <dataset-visualizer [dataset]="extractedDatasets[count]">
                                </dataset-visualizer>
                            </div>
                        <div class="col-sm-1"><span class="float-right" (click)="goRight()">
                            <img width="25em" src="/assets/images/chevron-right.svg"/></span>
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
            <div class="col-sm-10" align="center">
                <h4>{{'FILTERLIVEEDITINGCOMPONENT.NODATA' | translate}}</h4>
            </div>
        </ng-template>
    `
})

export class ExampleMessageComponent implements OnInit {
    @Input() public extractedDatasets: Dataset[];
    @Output() public currentExampleDataset: EventEmitter<Dataset> = new EventEmitter();
    private extractFinish$: Observable<boolean>;
    private count: number;
    private isReady$: Observable<boolean>;
    private noDataAvailable: boolean = false;

    constructor(private store: Store<any>) {
        this.isReady$ = this.store.select(getExtractFinish);
        this.extractFinish$ = this.store.select(getExtractFinish);
    }

    public ngOnInit(): void {
        if (this.extractedDatasets.length == 0) {
            this.noDataAvailable = true;
        }
        this.count = 0;
    }

    private goLeft() {
        if (this.count !== this.extractedDatasets.length - 1) {
            this.count += 1;
            const dataset: Dataset = this.extractedDatasets[this.count];
            this.currentExampleDataset.emit(dataset);
        }
    }

    private goRight() {
        if (this.count !== 0) {
            this.count -= 1;
            const dataset: Dataset = this.extractedDatasets[this.count];
            this.currentExampleDataset.emit(dataset);
        }
    }
}
