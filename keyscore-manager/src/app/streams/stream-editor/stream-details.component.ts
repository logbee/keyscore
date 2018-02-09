import {Component, EventEmitter, Input, Output} from "@angular/core";
import {StreamModel} from "../streams.model";

@Component({
    selector: 'stream-details',
    template: `
        <div class="card">
            <div class="card-header">
                <div class="d-flex justify-content-between">
                    <div>
                        <h4 *ngIf="locked" class="font-weight-bold">{{stream.name}}</h4>
                        <input *ngIf="!locked" placeholder="Name" [(ngModel)]="stream.name"/>
                    </div>
                    <div>
                        <button *ngIf="locked" type="button" class="btn btn-primary mr-1" (click)="startStreamEditing()">Edit</button>
                        <button *ngIf="!locked" type="button" class="btn btn-success" (click)="saveStreamEditing()">Save</button>
                        <button *ngIf="!locked" type="button" class="btn btn-secondary" (click)="cancelStreamEditing()">Cancel</button>
                    </div>
                </div>
                <div>
                    <small class="">{{stream.id}}</small>
                </div>
            </div>
            <div class="card-body">
                <label *ngIf="locked">{{stream.description}}</label>
                <textarea *ngIf="!locked" class="form-control" placeholder="Description" rows="4" [(ngModel)]="stream.description"></textarea>
            </div>
            <div class="card-footer d-flex justify-content-between">
                <div *ngIf="!locked">
                    <button type="button" class="btn btn-primary" (click)="addFilter()">Add Filter</button>
                </div>
                <div *ngIf="!locked">
                    <button type="button" class="btn btn-danger" (click)="deleteStream()">Delete</button>
                </div>
            </div>
        </div>
    `
})
export class StreamDetailsComponent {

    @Input() stream: StreamModel;

    @Output() update: EventEmitter<StreamModel> = new EventEmitter();
    @Output() reset: EventEmitter<StreamModel> = new EventEmitter();
    @Output() delete: EventEmitter<StreamModel> = new EventEmitter();
    @Output() lock: EventEmitter<StreamModel> = new EventEmitter();
    @Output() unlock: EventEmitter<StreamModel> = new EventEmitter();

    locked: boolean = true;

    constructor() {

    }

    deleteStream() {
        this.delete.emit(this.stream)
    }

    startStreamEditing() {
        this.locked = false;
    }
    saveStreamEditing() {
        this.locked = true;
        this.update.emit(this.stream)
    }

    cancelStreamEditing() {
        this.locked = true;
        this.reset.emit(this.stream)
    }
}