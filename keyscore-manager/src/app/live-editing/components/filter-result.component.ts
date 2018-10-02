import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Observable} from "rxjs/index";
import {Store} from "@ngrx/store";
import {selectcurrentDatasetCounter, selectResultAvailable} from "../filter.reducer";
import {Dataset} from "../../models/dataset/Dataset";

@Component({
    selector: "filter-result",
    template: `

        <mat-card>
            <mat-card-header class="fix-div">
                <div class="container" fxFlexFill="" fxLayout="row" fxLayout.xs="column">
                    <div fxFlexAlign="start" fxFlex="15%">
                        <mat-card-title><h1 class="mat-headline font-weight-bold">{{'FILTERLIVEEDITINGCOMPONENT.RESULT' | translate}}</h1></mat-card-title>
                    </div>
                    <div fxFlex>
                    <span (click)="goLeft()">
                            <button mat-icon-button color="none">
                                <mat-icon class="font-weight-bold">chevron_left</mat-icon>
                            </button>                          </span>
                        {{count + 1}}/ {{(resultDatasets$ | async)?.length}}
                        <span (click)="goRight()">
                            <button mat-icon-button color="none">
                                <mat-icon class="font-weight-bold">chevron_right</mat-icon>
                            </button>      
                        </span>
                    </div>
                </div>
            </mat-card-header>
            <mat-card-content>
                <!--<div align="center" *ngIf="(loading$ | async); else loading">-->
                    <dataset-visualizer [dataset]="(resultDatasets$ | async)[count]">
                    </dataset-visualizer>
                <!--</div>-->
            </mat-card-content>
        </mat-card>
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
    @Output() public currentDatasetCounter: EventEmitter<number> = new EventEmitter();
    private loading$: Observable<boolean>;
    private count$: Observable<number>;
    private count: number;
    private numberOfDatasets: number;

    constructor(private store: Store<any>) {
        this.loading$ = this.store.select(selectResultAvailable);
        this.count$ = this.store.select(selectcurrentDatasetCounter);
    }

    public ngOnInit(): void {
        this.resultDatasets$.subscribe((datasets) => {
            this.numberOfDatasets = datasets.length;
        });

        this.count$.subscribe((count) => {
            this.count = count;
        })

    }

    private goLeft() {
        if (this.count == 0) {
            this.count = this.numberOfDatasets - 1;
            this.emitCounter(this.count)
        } else  {
            this.count -= 1;
            this.emitCounter(this.count)
        }
    }

    private goRight() {
        if (this.count == this.numberOfDatasets - 1) {
            this.count = 0;
            this.emitCounter(this.count)
        } else  {
            this.count += 1;
            this.emitCounter(this.count)
        }
    }

    private emitCounter(count: number) {
        this.currentDatasetCounter.emit(count)
    }

}
