import {Component, Input} from "@angular/core";
import {Dataset} from "../../../models/filter-model/dataset/Dataset";

@Component({
    selector: "dataset-visualizer",
    template: `
        <div class="row">
            <div class="col-sm-2">
                <strong class="text-muted">id:</strong>
            </div>
            <div class="col-10">
                <small>{{dataset?.records[0].id}}</small>
            </div>
        </div>
        <div class="row">
            <div class="col-sm-2">
                <strong class="text-muted">records:</strong>
            </div>
            <div class="col-10">
                           <span (click)="toggleRecords()"><img width="16em"
                                                                src="/assets/images/chevron-down-dark.svg"/></span>
                <div class="col-sm-2"></div>
                <div class="col-sm-10">
                    <div class="row" *ngIf="showRecords">
                        <div class="col-sm-2">
                            <strong class="text-muted">payload:</strong>
                        </div>
                        <div class="col-sm-10">
                            <span (click)="togglePayload()"><img width="16em"
                                                                 src="/assets/images/chevron-down-dark.svg"/></span>
                        </div>
                        <div class="col-sm-2"></div>
                        <div class="col-sm-10" *ngIf="showPayload">
                            <div class="row">
                                <div class="col-sm-2">
                                    <strong class="text-muted">
                                        name </strong>
                                </div>
                                <div class="col-sm-10">
                                    <small>{{dataset?.records[0].payload.message.name}}</small>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-2">
                                    <strong class="text-muted">message: </strong>
                                </div>
                                <div class="col-sm-10">
                                    <small>{{dataset?.records[0].payload.message.value}}</small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})

export class DatasetVisualizer {
    @Input() public dataset: Dataset;
    public showPayload: boolean = true;
    public showRecords: boolean = true;

    private togglePayload() {
        this.showPayload = this.showPayload === false;
    }

    private toggleRecords() {
        this.showRecords = this.showRecords === false;
    }
}
