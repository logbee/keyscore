import {Component, Input} from "@angular/core";
import {FilterConfiguration} from "../../../pipelines.model";

@Component({
    selector: "filter-description",
    template: `
        <div class="card">
            <div class="card-header alert-light font-weight-bold">
                {{'FILTERLIVEEDITINGCOMPONENT.FILTERDESCRIPTION_TITLE' | translate}}
            </div>
            <div class="card-body">
                <table class="table table-condensed">
                    <thead>
                    <tr>
                        <th> {{'FILTERLIVEEDITINGCOMPONENT.NAME' | translate}}</th>
                        <th> {{'FILTERLIVEEDITINGCOMPONENT.DESCRIPTION' | translate}}</th>
                        <th> {{'FILTERLIVEEDITINGCOMPONENT.ID' | translate}}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>{{currentFilter.descriptor.displayName}}</td>
                        <td>{{currentFilter.descriptor.description}}</td>
                        <td>{{currentFilter.id}}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    `
})

export class FilterDescriptionComponent {

    @Input() public currentFilter: FilterConfiguration;

}
