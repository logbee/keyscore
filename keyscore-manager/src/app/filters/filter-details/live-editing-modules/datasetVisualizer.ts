import {Component, Input, OnInit} from "@angular/core";
import {Dataset} from "../../../models/dataset/Dataset";

@Component({
    selector: "dataset-visualizer",
    template: `
        <!--<table mat-table datasrc="datset" class="mat-elevation-z8">-->
            <!--<ng-container matRowDef="Field name">-->
                <!--<mat-header-cell *matHeaderCellDef>Field name</mat-header-cell>-->
                <!--<mat-cell *matCellDef="let record of dataset?.records">test</mat-cell>-->
            <!--</ng-container>-->
        <!--</table>-->
        <div *ngFor="let record of dataset?.records">
            <table class="table table-bordered">
                <tr *ngFor="let field of record.fields" [ngSwitch]="field.value.jsonClass">
                    <th>
                        <small>{{field.name}}</small>
                    </th>
                    <td *ngSwitchCase="'TextValue'">
                        <small>{{field.value.value}}</small>
                    </td>
                    <td *ngSwitchCase="'NumberValue'">
                        <small>
                            {{field.value.value}}
                        </small>
                    </td>
                    <td *ngSwitchCase="'DecimalValue'">
                        <small>
                            {{field.value.value}}
                        </small>
                    </td>
                    <td *ngSwitchCase="'TimestampValue'">
                        <small>
                            {{convertToDateTime(field.value.seconds)}}
                        </small>
                    </td>
                    <td *ngSwitchCase="'DurationValue'">
                        <small>
                            {{convertToDateTime(field.value.seconds)}}
                        </small>
                    </td>
                    <td>
                        <small>
                            {{field.value.jsonClass}}
                        </small>
                    </td>
                </tr>
            </table>
        </div>
    `
})

export class DatasetVisualizer implements OnInit {
    @Input() public dataset: Dataset;

    ngOnInit(): void {

    }

    convertToDateTime(seconds: number) {
        const dateTime = new Date(1970, 0, 1);
        dateTime.setSeconds(seconds);
        return dateTime;
    }

}
