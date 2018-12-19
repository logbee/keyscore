import {Location} from "@angular/common";
import {Component, OnDestroy, OnInit} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {isSpinnerShowing} from "../../common/loading/loading.reducer";
import {Go} from "../../router/router.actions";
import {
    DeletePipelineAction,
    LoadFilterDescriptorsAction,
    ResetPipelineAction,
    UpdatePipelineAction
} from "../actions/pipelines.actions";
import {share, takeUntil} from "rxjs/internal/operators";
import {InternalPipelineConfiguration} from "../../models/pipeline-model/InternalPipelineConfiguration";
import {ResolvedFilterDescriptor} from "../../models/descriptors/FilterDescriptor";
import {Configuration} from "../../models/common/Configuration";
import {EditingPipelineModel} from "../../models/pipeline-model/EditingPipelineModel";
import {PipelyKeyscoreAdapter} from "../../services/pipely-keyscore-adapter.service";
import {BlockDescriptor} from "./pipely/models/block-descriptor.model";
import {isError, selectErrorMessage, selectHttpErrorCode} from "../../common/error/error.reducer";
import {getEditingPipeline, getFilterDescriptors} from "../index";
import {Ref} from "../../models/common/Ref";
import {ExtractFromSelectedBlock} from "../actions/preview.actions";
import {DraggableModel} from "./pipely/models/draggable.model";

@Component({
    selector: "pipeline-editor",
    template: `
        <loading-full-view *ngIf="((isLoading$|async) && showBigLoadingViewOnLoading); else error"></loading-full-view>

        <ng-template #error>
            <error-component *ngIf="(errorState$ | async);else fullComponent"
                             [httpError]="(errorStatus$ | async)"
                             [message]="(errorMessage$ | async)">
            </error-component>
        </ng-template>

        <ng-template #fullComponent>
            <header-bar [title]="'Pipeline Editor'" [showSave]="true" [showRun]="true" [showDelete]="true"
                        [showInspect]="true"
                        [isLoading]="isLoading$|async"
                        (onSave)="savePipelineSource$.next()"
                        (onRun)="runPipelineSource$.next()"
                        (onInspect)="inspectToggle($event)"
            ></header-bar>

            <pipely-workspace [runTrigger$]="runPipeline$" [saveTrigger$]="savePipeline$"
                              [inspectTrigger$]="runInspect$"
                              [pipeline]="(pipeline$ | async)"
                              [blockDescriptors]="blockDescriptorSource$|async"
                              (onUpdatePipeline)="updatePipeline($event)"
                              (onRunPipeline)="runPipeline($event)"
                              (onSelectBlock)="selectBlock($event)"
                              fxFill></pipely-workspace>
        </ng-template>


    `,
})
export class PipelineEditorComponent implements OnInit, OnDestroy {
    public pipeline$: Observable<EditingPipelineModel>;
    public filterDescriptors$: Observable<ResolvedFilterDescriptor[]>;
    public isLoading$: Observable<boolean>;

    private alive: Subject<void> = new Subject();

    public savePipelineSource$: Subject<void> = new Subject<void>();
    public savePipeline$: Observable<void> = this.savePipelineSource$.asObservable();

    public runPipelineSource$: Subject<void> = new Subject<void>();
    public runPipeline$: Observable<void> = this.runPipelineSource$.asObservable();

    public runInspectSource$: Subject<boolean> = new Subject<boolean>();
    public runInspect$: Observable<boolean> = this.runInspectSource$.asObservable();

    public blockDescriptorSource$: BehaviorSubject<BlockDescriptor[]> = new BehaviorSubject<BlockDescriptor[]>([]);

    public storeEditingPipeline: EditingPipelineModel;

    public errorState$: Observable<boolean>;
    public errorStatus$: Observable<string>;
    public errorMessage$: Observable<string>;

    private showBigLoadingViewOnLoading = true;
    private selectedBlock: DraggableModel;
    private amount: number = 10;
    private where: string = "after";


    constructor(private store: Store<any>, private location: Location, private pipelyAdapter: PipelyKeyscoreAdapter) {
    }

    inspectToggle(flag: boolean) {
        if (flag) {
            this.runInspectSource$.next(true);
        } else {
            this.runInspectSource$.next(false);
        }
    }

    ngOnInit() {
        this.showBigLoadingViewOnLoading = true;

        this.store.dispatch(new LoadFilterDescriptorsAction());

        this.filterDescriptors$ = this.store.pipe(select(getFilterDescriptors), takeUntil(this.alive));
        this.isLoading$ = this.store.pipe(select(isSpinnerShowing), share());
        this.pipeline$ = this.store.pipe(select(getEditingPipeline), takeUntil(this.alive));

        this.pipeline$.subscribe(pipe => {
            this.storeEditingPipeline = pipe;
        });

        this.filterDescriptors$.subscribe(descriptors => {
            this.blockDescriptorSource$.next(descriptors.map(descriptor =>
                this.pipelyAdapter.resolvedParameterDescriptorToBlockDescriptor(descriptor)))
        });

        this.runInspectSource$.subscribe(flag => {
            if (flag) {
                //Extract from currently selected block
                if (this.selectedBlock != undefined) {
                    if (this.isSink(this.selectedBlock)) {
                        this.where = "before";
                    }
                    this.store.dispatch(new ExtractFromSelectedBlock(this.selectedBlock.blueprintRef.uuid, this.where, this.amount));
                } else {
                    // extract from source
                    this.store.dispatch(new ExtractFromSelectedBlock(this.storeEditingPipeline.blueprints[0].ref.uuid, this.where, this.amount));
                }
            }
        });
        this.errorState$ = this.store.pipe(select(isError));
        this.errorStatus$ = this.store.pipe(select(selectHttpErrorCode));
        this.errorMessage$ = this.store.pipe(select(selectErrorMessage));
    }

    public ngOnDestroy() {
        this.alive.next();
    }

    public deletePipeline(pipeline: EditingPipelineModel) {
        this.store.dispatch(new DeletePipelineAction(pipeline.pipelineBlueprint.ref.uuid));
        this.location.back();
    }

    public updatePipeline(pipeline: EditingPipelineModel) {
        this.store.dispatch(new UpdatePipelineAction(pipeline));
    }

    public runPipeline(pipeline: EditingPipelineModel) {
        this.store.dispatch(new UpdatePipelineAction(pipeline, true));
        this.showBigLoadingViewOnLoading = false;
    }

    public resetPipeline(pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new ResetPipelineAction(pipeline.id));
    }

    public callLiveEditing(filter: Configuration) {
        this.store.dispatch(new Go({path: ["pipelines/filter/" + filter.ref.uuid + "/"]}));
    }

    public selectBlock(draggableModel: DraggableModel) {
        this.selectedBlock = draggableModel;
    }

    public isSink(draggable: DraggableModel) {
        return draggable.blockDescriptor.nextConnection === undefined;
    }

}
