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
                <tr *ngFor="let field of record.fields">
                    <th>{{field.name}}</th>
                    <div [ngSwitch]="field.value.jsonClass">
                        <td *ngSwitchCase="'TextValue'">
                            {{field.value.value}}
                        </td>
                    </div>
                    <div [ngSwitch]="field.value.jsonClass">
                        <td *ngSwitchCase="'NumberValue'">
                            {{field.value.value}}
                        </td>
                    </div>
                    <div [ngSwitch]="field.value.jsonClass">
                        <td *ngSwitchCase="'DecimalValue'">
                            <!--{{todo}}-->
                        </td>
                    </div>
                    <div [ngSwitch]="field.value.jsonClass">
                        <td *ngSwitchCase="'TimestampValue'">
                            <!--{{todo}}-->
                        </td>
                    </div>
                    <div [ngSwitch]="field.value.jsonClass">
                        <td *ngSwitchCase="'DurationValue'">
                            <!--{{todo}}-->
                        </td>
                    </div>
                    <td>{{field.value.jsonClass}}</td>
                </tr>
            </table>
        </div>
    `
})

export class DatasetVisualizer {
    @Input() public dataset: Dataset;
}
