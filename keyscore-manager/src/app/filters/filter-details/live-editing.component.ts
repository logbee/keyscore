import {Component} from "@angular/core";

@Component({
    selector: 'live-editing',
    template: `
        <div class="col-12">
            <div class="card">
                <div class="card-header alert-info">
                    Live Editing
                </div>
                <div class="card-body badge-light">
                    <div class="card">
                        <div class="card-header alert-light font-weight-bold">
                            Filter Description
                        </div>
                        <div class="card-body">
                            <table class="table table-condensed">
                                <thead>
                                <tr>
                                    <th>FilterName</th>
                                    <th>FilterDescription</th>
                                    <th>FilterPattern</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td>GrokFilter</td>
                                    <td>Filter that structures messages with Regex.</td>
                                    <td>""</td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="card mt-3">
                        <div class="card-header alert-light font-weight-bold">Example Message</div>
                        <div class="card-body">
                            <div class="form-group">
                                <textarea class="form-control" rows="5"></textarea>
                            </div>

                        </div>
                    </div>
                    <div class="card mt-3">
                        <div class="card-header alert-light font-weight-bold">Regex Pattern</div>
                        <div class="card-body">
                            <div class="form-group">
                                <textarea class="form-control" rows="1"></textarea>
                            </div>
                                <button class="float-right primary btn-info">Apply Filter</button>
                        </div>
                    </div>

                    <div class="card mt-3">
                        <div class="card-header alert-light font-weight-bold">Result</div>
                        <div class="card-body">
                            <div class="form-group">
                                <table class="table table-condensed">
                                    <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>name</th>
                                        <th>value</th>
                                        <th>type</th>
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
                    <button class="mt-3 float-right primary btn-success">Save/Use Filter</button>
                </div>
            </div>
        </div>
    `
})

export class LiveEditingComponent {

}