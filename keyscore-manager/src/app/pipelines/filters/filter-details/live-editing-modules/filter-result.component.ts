import {Component, Input} from "@angular/core";

@Component({
    selector: "filter-result",
    template: `
        <div class="card mt-3">
            <div class="card-header alert-light font-weight-bold">
                {{'FILTERLIVEEDITINGCOMPONENT.RESULT' | translate}}
            </div>
            <div class="card-body">
                <div class="form-group">
                    <table class="table table-condensed">
                        <thead>
                        <tr>
                            <th> {{'FILTERLIVEEDITINGCOMPONENT.NUMBER' | translate}}</th>
                            <th> {{'FILTERLIVEEDITINGCOMPONENT.NAME' | translate}}</th>
                            <th> {{'FILTERLIVEEDITINGCOMPONENT.VALUE' | translate}}</th>
                            <th> {{'FILTERLIVEEDITINGCOMPONENT.TYPE' | translate}}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>1</td>
                            <td>aggregatetField</td>
                            <td>2.35</td>
                            <td>Integer</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    `
})

export class FilterResultComponent {

}
