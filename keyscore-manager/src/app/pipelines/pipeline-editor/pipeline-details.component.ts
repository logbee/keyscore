import {Component, EventEmitter, Input, Output} from "@angular/core";
import {Observable} from "rxjs";
import {InternalPipelineConfiguration} from "../../models/pipeline-model/InternalPipelineConfiguration";

@Component({
    selector: "pipeline-details",
    template: `
        <div class="card">
            <div class="card-header">
                <div class="d-flex justify-content-between">
                    <div>
                        <h4 *ngIf="(locked$|async)" class="font-weight-bold">{{pipeline.name}}</h4>
                        <input class="form-control" *ngIf="!(locked$ | async)" placeholder="Name"
                               [(ngModel)]="pipeline.name"/>
                    </div>
                </div>
                <div>
                    <small class="">{{pipeline.id}}</small>
                </div>
            </div>
            <div class="card-body">
                <label *ngIf="(locked$ | async)">{{pipeline.description}}</label>
                <textarea *ngIf="!(locked$ | async)" class="form-control" placeholder="Description" rows="4"
                          [(ngModel)]="pipeline.description"></textarea>
            </div>
            <div class="card-footer d-flex justify-content-between">
                <div *ngIf="!(locked$ | async)">
                    <button type="button" class="btn btn-danger" (click)="deletePipeline()">
                        <img src="/assets/images/ic_delete_white_24px.svg" alt="Remove"/></button>
                </div>

                <div>
                    <button *ngIf="(locked$ | async)" type="button" class="btn btn-primary mr-1"
                            (click)="startPipelineEditing()">Edit
                    </button>
                    <button *ngIf="!(locked$ | async)" type="button" class="btn btn-secondary mr-1"
                            (click)="cancelPipelineEditing()">
                        <img src="/assets/images/ic_cancel_white_24px.svg" alt="Cancel"/>
                    </button>
                    <button *ngIf="!(locked$ | async)" type="button" class="btn btn-success"
                            (click)="savePipelineEditing()"><img src="/assets/images/ic_save_white.svg" alt="Save"/>
                    </button>
                </div>
            </div>
        </div>
    `
})
export class PipelineDetailsComponent {

    @Input() public pipeline: InternalPipelineConfiguration;
    @Input() public locked$: Observable<boolean>;

    @Output() private update: EventEmitter<InternalPipelineConfiguration> = new EventEmitter();
    @Output() private reset: EventEmitter<InternalPipelineConfiguration> = new EventEmitter();
    @Output() private delete: EventEmitter<InternalPipelineConfiguration> = new EventEmitter();
    @Output() private lock: EventEmitter<InternalPipelineConfiguration> = new EventEmitter();
    @Output() private unlock: EventEmitter<InternalPipelineConfiguration> = new EventEmitter();

    public deletePipeline() {
        this.delete.emit(this.pipeline);
    }

    public startPipelineEditing() {
        this.unlock.emit(this.pipeline);
    }

    public savePipelineEditing() {
        this.lock.emit(this.pipeline);
        this.update.emit(this.pipeline);
    }

    public cancelPipelineEditing() {
        this.lock.emit(this.pipeline);
        this.reset.emit(this.pipeline);
    }
}
