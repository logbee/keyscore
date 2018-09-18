import {Location} from "@angular/common";
import {Component, OnDestroy} from "@angular/core";
import {Store} from "@ngrx/store";
import {Observable, Subject} from "rxjs";
import {isSpinnerShowing} from "../../common/loading/loading.reducer";
import {Go} from "../../router/router.actions";
import {
    DeletePipelineAction,
    LoadFilterDescriptorsAction,
    ResetPipelineAction,
    UpdatePipelineAction
} from "../pipelines.actions";
import {map, share} from "rxjs/internal/operators";
import {isMenuExpanded} from "../../common/sidemenu/sidemenu.reducer";
import {InternalPipelineConfiguration} from "../../models/pipeline-model/InternalPipelineConfiguration";
import {ResolvedFilterDescriptor} from "../../models/descriptors/FilterDescriptor";
import {
    getEditingPipeline,
    getFilterCategories,
    getFilterDescriptors,
    getLastUpdateSuccess
} from "../pipelines.reducer";
import {Configuration} from "../../models/common/Configuration";
import {EditingPipelineModel} from "../../models/pipeline-model/EditingPipelineModel";
import {ResolvedCategory} from "../../models/descriptors/Category";

@Component({
    selector: "pipeline-editor",
    template: `
        <header-bar [title]="'Pipeline Editor'"></header-bar>

        <loading-full-view *ngIf="isLoading$|async; else editor"></loading-full-view>

        <ng-template #editor>
            
            <pipely-workspace [pipeline]="(pipeline$ | async)" fxFill=""></pipely-workspace>

            <alert [level]="'success'" [message]="'BLOCKLY.SAVE_SUCCESS'"
                   [trigger$]="successAlertTrigger$"></alert>
            <alert [level]="'danger'" [message]="'BLOCKLY.SAVE_FAILURE'"
                   [trigger$]="failureAlertTrigger$"></alert>
        </ng-template>
    `,
})
export class PipelineEditorComponent implements OnDestroy {
    public pipeline$: Observable<EditingPipelineModel>;
    public filterDescriptors$: Observable<ResolvedFilterDescriptor[]>;
    public categories$: Observable<ResolvedCategory[]>;
    public isLoading$: Observable<boolean>;
    public isMenuExpanded$: Observable<boolean>;
    public updateSuccess$: Observable<boolean[]>;

    public successAlertTrigger$: Observable<boolean>;
    public failureAlertTrigger$: Observable<boolean>;

    private alive: Subject<void> = new Subject();

    constructor(private store: Store<any>, private location: Location) {
        this.store.dispatch(new LoadFilterDescriptorsAction());

        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.categories$ = this.store.select(getFilterCategories);
        this.isLoading$ = this.store.select(isSpinnerShowing).pipe(share());
        this.updateSuccess$ = this.store.select(getLastUpdateSuccess).pipe(share());
        this.isMenuExpanded$ = this.store.select(isMenuExpanded);
        this.pipeline$ = this.store.select(getEditingPipeline);

        this.successAlertTrigger$ = this.updateSuccess$.pipe(map((success) => success[0]));
        this.failureAlertTrigger$ = this.updateSuccess$.pipe(
            map((successList) => !successList[0]));

    }

    public ngOnDestroy() {
        this.alive.next();
    }

    public deletePipeline(pipeline: EditingPipelineModel) {
        this.store.dispatch(new DeletePipelineAction(pipeline.pipelineBlueprint.ref.uuid));
        this.location.back();
    }

    public updatePipeline(pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new UpdatePipelineAction({
            id: pipeline.id,
            name: pipeline.name,
            description: pipeline.description,
            filters: pipeline.filters,
            isRunning: pipeline.isRunning
        }));
    }

    public resetPipeline(pipeline: InternalPipelineConfiguration) {
        this.store.dispatch(new ResetPipelineAction(pipeline.id));
    }

    public callLiveEditing(filter: Configuration) {
        this.store.dispatch(new Go({path: ["pipelines/filter/" + filter.ref.uuid + "/"]}));
    }
}
