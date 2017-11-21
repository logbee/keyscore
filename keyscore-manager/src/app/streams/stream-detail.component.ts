import { Component } from '@angular/core';

@Component({
    selector: 'keyscore-stream-detail',
    template: `
            <div class="row justify-content-center">
                <div class="col-4">
                    <div class="card">
                        <div class="card-header">
                            <h4 class="">Stream</h4>
                        </div>
                        <div class="card-body">
                            <label for="streamName" class="font-weight-bold">Name</label>
                            <input id="streamName" class="form-control" placeholder="Name" />
                            <label for="streamDescription" class="font-weight-bold">Description</label>
                            <textarea id="streamDescription" class="form-control" placeholder="Description" rows="3"></textarea>
                        </div>
                    </div>
                </div>
                <div class="col-8">
                    <div class="card">
                        <div class="card-header">
                            <div class="d-flex justify-content-between">
                                <h4>Filters</h4>
                                <button type="button" class="btn btn-success" routerLink="/filter/add">Add</button>
                            </div>
                        </div>
                        <div class="card-body">
                            <div class="list-group">
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <h6 class="font-weight-bold">Filter 1</h6>
                                            <small>Kafka Input.</small>
                                        </div>
                                        <div>
                                            <button type="button" class="btn btn-primary" routerLink="/filter/details">Edit</button>
                                            <button type="button" class="btn btn-danger">Remove</button>
                                        </div>
                                    </div>
                                </div>
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <h6 class="font-weight-bold">Filter 3</h6>
                                            <small>Awesome filter!</small>
                                        </div>
                                        <div>
                                            <button type="button" class="btn btn-primary" routerLink="/filter/details">Edit</button>
                                            <button type="button" class="btn btn-danger">Remove</button>
                                        </div>
                                    </div>
                                </div>
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <h6 class="font-weight-bold">Filter 3</h6>
                                            <small>Kafka output.</small>
                                        </div>
                                        <div>
                                            <button type="button" class="btn btn-primary" routerLink="/filter/details">Edit</button>
                                            <button type="button" class="btn btn-danger">Remove</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
    `
})

export class StreamDetailComponent {

}
