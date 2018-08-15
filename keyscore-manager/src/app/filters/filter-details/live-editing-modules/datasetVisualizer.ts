import {Component, Input, OnInit} from "@angular/core";
import {Dataset} from "../../../models/filter-model/dataset/Dataset";

@Component({
    selector: "dataset-visualizer",
    template: `
        <div *ngFor="let record of dataset?.records">
            <table class="table table-bordered">
                <tr *ngFor="let field of record.fields"  [ngSwitch]="field.value.jsonClass">
                    <th>{{field.name}}</th>
                        <td *ngSwitchCase="'TextValue'">
                            {{field.value.value}}
                        </td>
                        <td *ngSwitchCase="'NumberValue'">
                            {{field.value.value}}
                        </td>
                        <td *ngSwitchCase="'DecimalValue'">
                            {{field.value.value}}
                        </td>
                        <td *ngSwitchCase="'TimestampValue'">
                           {{convertToDateTime(field.value.seconds)}}
                        </td>
                        <td *ngSwitchCase="'DurationValue'">
                            {{convertToDateTime(field.value.seconds)}}
                        </td>
                    <td>{{field.value.jsonClass}}</td>
                </tr>
            </table>
        </div>
    `
})

export class DatasetVisualizer implements OnInit{
    @Input() public dataset: Dataset;

    ngOnInit(): void {

    }

    convertToDateTime(seconds: number) {
        const dateTime = new Date(1970, 0, 1);
        dateTime.setSeconds(seconds);
        return dateTime;
    }

}
