import {Component, Input} from "@angular/core";
import {Dataset} from "../../../models/filter-model/dataset/Dataset";

@Component({
    selector: "dataset-visualizer",
    template: `
        <div *ngFor="let record of dataset?.records">
            <div class="row">
                <strong></strong>
            </div>
            <table class="table table-bordered">
                <tr *ngFor="let field of record.payload">
                    <th>{{field.name}}</th>
                    <td>{{field.value}}</td>
                    <td>{{field.jsonClass}}</td>
                </tr>
            </table>
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
