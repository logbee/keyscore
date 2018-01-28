import {Component} from '@angular/core'
import {ModalService} from "../../services/modal.service";
import {FilterDescriptor, FilterService, LOAD_FILTER_DESCRIPTORS} from "../../services/filter.service";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs/Observable";
import {AddFilterAction} from "./stream-editor.actions";
import {AppState} from "../../app.component";

@Component({
    selector: 'filter-chooser',
    template: `
        <div class="modal fade" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Filter Chooser</h5>
                        <button type="button" class="close" aria-label="Close" (click)="close()">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <div class="container-fluid">
                            <div class="row">
                                <div class="col">
                                    <div class="row">
                                        <div class="input-group">
                                            <div class="input-group-prepend">
                                                <img class="input-group-text" width="48em" src="/assets/images/magnifying-glass.svg"/>
                                            </div>
                                            <input type="text" class="form-control" placeholder="search..." aria-label="search">
                                        </div>
                                    </div>
                                    <div class="card mt-3">
                                        <div *ngFor="let descriptor of filterDescriptors$ | async" class="list-group-flush">
                                            <button class="list-group-item list-group-item-action" (click)="select(descriptor)">{{descriptor.displayName}}</button>
                                        </div>
                                    </div>
                                </div>
                                <div class="col ml-3">
                                    <div *ngIf="selectedFilterDescriptor" class="card">
                                        <div class="list-group-flush">
                                            <div class="list-group-item d-flex justify-content-between">
                                                <h4>{{selectedFilterDescriptor.displayName}}</h4>
                                                <button type="button" class="btn btn-success" (click)="add(selectedFilterDescriptor)">Add</button>
                                            </div>
                                            <div class="list-group-item">
                                                <div class="">
                                                    <div class="">
                                                        {{selectedFilterDescriptor.description}}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" (click)="close()">Close</button>
                    </div>
                </div>
            </div>
        </div>
    `,
    providers: [
        ModalService,
        FilterService
    ]
})

export class FilterChooser {

    private filterDescriptors$: Observable<FilterDescriptor[]>;
    private selectedFilterDescriptor: FilterDescriptor;

    constructor(private store: Store<AppState>, private modalService: ModalService) {
        this.filterDescriptors$ = this.store.select(state => state.filterDescriptors);
        this.store.dispatch({type: LOAD_FILTER_DESCRIPTORS});
    }

    select(filterDescriptor: FilterDescriptor) {
        this.selectedFilterDescriptor = filterDescriptor;
    }

    add(filterDescriptor: FilterDescriptor) {
        this.store.dispatch(new AddFilterAction(this.selectedFilterDescriptor));
    }

    close() {
        this.modalService.close()
    }
}