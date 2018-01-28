import {Component} from '@angular/core'
import {ModalService} from "../services/modal.service";
import {FilterDescriptor, FilterService, LOAD_FILTER_DESCRIPTORS} from "../services/filter.service";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs/Observable";
import {AddFilterAction, Stream} from "./stream.reducer";
import {AppState} from "../app.component";

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
                                            <span class="input-group-addon"><img src="../common/magnifying-glass.svg"/></span>
                                            <input type="text" class="form-control" placeholder="search..." aria-label="search">
                                        </div>
                                    </div>
                                    <div>
                                        <div *ngFor="let descriptor of filterDescriptors$ | async">
                                            <span>Name:</span><span>{{descriptor.name}}</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="col ml-3">
                                    <div class="row justify-content-between">
                                        <h4>GrokFilter</h4>
                                        <button type="button" class="btn btn-success">Add</button>
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
    private currentStream$: Observable<Stream>;

    constructor(private store: Store<AppState>, private modalService: ModalService) {
        this.filterDescriptors$ = this.store.select(state => state.filterDescriptors);
        this.currentStream$ = this.store.select(state => state.stream);
        this.store.dispatch({type: LOAD_FILTER_DESCRIPTORS});
    }

    add() {
        this.store.dispatch(new AddFilterAction(null))
    }

    close() {
        this.modalService.close()
    }
}