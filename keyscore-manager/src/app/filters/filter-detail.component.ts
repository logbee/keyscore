import { Component } from '@angular/core';
import namedRegexp = require("named-js-regexp");

export class FilterDetailsModel {
    id: string;
    name: string;
    message: string;
    regex: string;
    failure: boolean;
    results: Result[]
}

class Result {
    name: string;
    value: string;
    type: string;

    constructor(name: string, value : string, type: string) {
        this.name = name;
        this.value = value;
        this.type = type;
    }
}

@Component({
    selector: 'keyscore-filter-details',
    template: `
        <div class="row justify-content-center">
            <div class="col-8">
                <div class="card mt-3">
                    <div class="card-header">
                        <h4 class="">Filter</h4>
                    </div>
                    <div class="card-body">
                        <div class="mb-3">
                            <label for="exampleTextarea" class="font-weight-bold">Example</label>
                            <textarea id="exampleTextarea" [(ngModel)]="model.message" name="message"
                                      (ngModelChange)="onChanged()"
                                      class="form-control" placeholder="message" rows="3"></textarea>
                        </div>
                        <div class="card">
                            <div class="card-header">
                                <ul class="nav nav-tabs card-header-tabs">
                                    <li class="nav-item">
                                        <a class="nav-link active" href="#">RegEx</a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-link" href="#">Script</a>
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-link" href="#">Visual</a>
                                    </li>
                                </ul>
                            </div>
                            <div class="card-body">
                                <div>
                                    <div class="form-group">
                                        <textarea id="codeTextarea" [(ngModel)]="model.regex" name="regex"
                                                  (ngModelChange)="onChanged()"
                                                  [ngClass]="[model.failure ? 'border-danger' : 'border-success']"
                                                  class="form-control" placeholder="expression" rows="1"></textarea>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="mt-3 mb-3">
                            <button type="button" class="btn btn-primary">Save</button>
                            <button type="button" class="btn btn-secondary">Cancel</button>
                        </div>
                        <div class="card">
                            <div class="card-header"><h5>Result</h5></div>
                            <div class="card-body">
                                <table id="resultTable" class="table table-sm table-striped">
                                    <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Name</th>
                                        <th>Value</th>
                                        <th>Type</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr *ngFor="let result of model.results; let i = index">
                                        <th scope="row">{{i}}</th>
                                        <td>{{result.name}}</td>
                                        <td>{{result.value}}</td>
                                        <td>{{result.type}}</td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})

export class FilterDetailComponent {

    private static readonly NUMBER_REGEX = namedRegexp('^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?$');

    model: FilterDetailsModel = {
        id: '1',
        name: 'Test',
        message: 'record stopped for measurement position 3 with: {"MeasurementResult": {"result1": "0.255", "result2": "0.441"}}',
        regex: 'record\\s(?<status>.*?)\\s.*position\\s(?<location>.?).*result1\\":\\s\\"(?<x_offset>.*)\\",.*"(?<z_offset>.*?)\\"',
        failure: false,
        results: []
    };

    private onChanged() {

        this.model.results = [];
        this.model.failure = true;

        try {
            const regex = namedRegexp(this.model.regex);
            const matches = regex.execGroups(this.model.message);

            if (matches) {

                for (let key in matches) {

                    let value = matches[key];
                    let type = 'Text';

                    if (FilterDetailComponent.NUMBER_REGEX.test(value)) {
                        type = 'Number'
                    }

                    this.model.results.push(new Result(key, matches[key], type))
                }

                this.model.failure = false;
            }
        }
        catch (e) {

        }
    }
}

// Examples:
//
// ClosedOut WebSocketServletConnectionRFC6455 p=WebSocketParserRFC6455@3189cd state=OPCODE buffer=null g=WebSocketGeneratorRFC6455@4dff2a closed=false buffer=-1 1000 Idle for 20117ms > 20000ms
//
// record stopped for measurement position 3 with: {"MeasurementResult": {"result1": "0.255", "result2": "0.441"}}
// record\s(?<status>.*?)\s.*position\s(?<location>.?).*result1\":\s\"(?<x_offset>.*)\",.*"(?<z_offset>.*?)\"
//
// json: {"memory":{"used":130,"free":117,"total":248,"max":248,"unit":"MB"}}