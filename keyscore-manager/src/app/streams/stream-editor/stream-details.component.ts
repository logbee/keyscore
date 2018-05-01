import {Component, EventEmitter, Input, Output} from "@angular/core";
import {StreamModel} from "../streams.model";
import {Observable} from "rxjs/Observable";

@Component({
    selector: 'stream-details',
    template: `
        <div class="card">
            <div class="card-header">
                <div class="d-flex justify-content-between">
                    <div>
                        <h4 *ngIf="(locked$|async)" class="font-weight-bold">{{stream.name}}</h4>
                        <input class="form-control" *ngIf="!(locked$ | async)" placeholder="Name" [(ngModel)]="stream.name"/>
                    </div>

                </div>
                <div>
                    <small class="">{{stream.id}}</small>
                </div>
            </div>
            <div class="card-body">
                <label *ngIf="(locked$ | async)">{{stream.description}}</label>
                <textarea *ngIf="!(locked$ | async)" class="form-control" placeholder="Description" rows="4"
                          [(ngModel)]="stream.description"></textarea>
            </div>
            <div class="card-footer d-flex justify-content-between">
                <div *ngIf="!(locked$ | async)">
                        <button type="button" class="btn btn-danger" (click)="deleteStream()"><img src="/assets/images/ic_delete_white_24px.svg"
                                                                                                   alt="Remove"/></button>
                </div>

                <div>
                    <button *ngIf="(locked$ | async)" type="button" class="btn btn-primary mr-1"
                            (click)="startStreamEditing()">Edit
                    </button>
                    <button *ngIf="!(locked$ | async)" type="button" class="btn btn-secondary"
                            (click)="cancelStreamEditing()"><img src="/assets/images/ic_cancel_white_24px.svg" alt="Cancel"/>
                    </button>
                    <button *ngIf="!(locked$ | async)" type="button" class="btn btn-success"
                            (click)="saveStreamEditing()"><img src="/assets/images/ic_save_white.svg" alt="Save"/>
                    </button>
                    
                </div>
            </div>
        </div>
    `
})
export class StreamDetailsComponent {

    @Input() stream: StreamModel;
    @Input() locked$: Observable<boolean>;

    @Output() update: EventEmitter<StreamModel> = new EventEmitter();
    @Output() reset: EventEmitter<StreamModel> = new EventEmitter();
    @Output() delete: EventEmitter<StreamModel> = new EventEmitter();
    @Output() lock: EventEmitter<StreamModel> = new EventEmitter();
    @Output() unlock: EventEmitter<StreamModel> = new EventEmitter();

    //locked: boolean = true;

    constructor() {

    }

    deleteStream() {
        this.delete.emit(this.stream)
    }

    startStreamEditing() {
        //this.locked = false;
        this.unlock.emit(this.stream);
    }

    saveStreamEditing() {
        //this.locked = true;
        this.lock.emit(this.stream);
        this.update.emit(this.stream)
    }

    cancelStreamEditing() {
        //this.locked = true;
        this.lock.emit(this.stream);
        this.reset.emit(this.stream)
    }
}