import {Component, Input} from "@angular/core";
import {Dataset} from "../../../models/filter-model/dataset/Dataset";
import {Observable} from "rxjs/index";

@Component({
    selector: "example-message",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold" style="color: black;">
                {{'FILTERLIVEEDITINGCOMPONENT.EXAMPLE_MESSAGE' | translate}}
            </div>
            <div class="card-body">
                <div class="ml-3">
                    <div class="row">
                        <div class="col-sm-2 mb-2">
                            <span (click)="toggleRecords()"><img width="16em"
                              src="/assets/images/chevron-left.svg"/></span></div>
                        <div class="col-sm-8 mb-2"><dataset-visualizer [dataset]="dataset">
                        </dataset-visualizer></div>
                        <div class="col-sm-2 mb-2"><span (click)="toggleRecords()"><img width="16em"
                                      src="/assets/images/chevron-right.svg"/></span></div>
                    </div>
                </div>
            </div>
        </div>
    `
})

export class ExampleMessageComponent {
    @Input() public dataset: Dataset[];
}
