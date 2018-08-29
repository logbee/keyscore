import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Observable} from "rxjs/index";
import {Store} from "@ngrx/store";
import {selectcurrentDatasetCounter, selectExtractFinish, selectUpdateConfigurationFlag} from "../../filter.reducer";
import {Dataset} from "../../../models/filter-model/dataset/Dataset";
import "../../styles/filterstyle.css";

@Component({
    selector: "example-message",
    template: `
        <mat-card>
            <mat-card-header class="fix-div">
                <div class="container" fxFlexFill="" fxLayout="row" fxLayout.xs="column">
                    <div fxFlexAlign="start" fxFlex="30%">
                        <mat-card-title><h1 class="mat-headline font-weight-bold">{{'FILTERLIVEEDITINGCOMPONENT.EXAMPLE_MESSAGE' | translate}}</h1></mat-card-title>
                    </div>
                    <div fxFlex>
                    <span (click)="goLeft()">
                        <img width="18em" src="/assets/images/chevron-left.svg"/>
                    </span>
                        {{count + 1}}/ {{(extractedDatasets$ | async)?.length}}
                        <span (click)="goRight()">
                    <img width="18em" src="/assets/images/chevron-right.svg"/>
                    </span>
                    </div>
                </div>
            </mat-card-header>
            <mat-card-content>
                <div *ngIf="(!noDataAvailable); else noData">
                    <div *ngIf="(extractFinish$ | async); else loading">
                        <dataset-visualizer [dataset]="(extractedDatasets$ | async)[count]">
                        </dataset-visualizer>
                    </div>
                </div>
            </mat-card-content>
        </mat-card>
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
        } else {
            this.count -= 1;
            this.emitCounter(this.count)
        }
    }

    private goRight() {
        if (this.count == this.numberOfDatasets - 1) {
            this.count = 0;
            this.emitCounter(this.count);
        } else {
            this.count += 1;
            this.emitCounter(this.count)
        }
    }

    private emitCounter(count: number) {
        this.currentDatasetCounter.emit(count)
    }
}
