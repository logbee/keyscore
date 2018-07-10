import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {Blockly} from "node-blockly/browser";
import {BehaviorSubject, combineLatest, Observable} from "rxjs";
import {delay, filter, map, takeWhile} from "rxjs/internal/operators";
import {ToolBarBuilderService} from "../../../services/blockly/toolbarbuilder.service";
import {FilterDescriptor, InternalPipelineConfiguration, PipelineConfiguration} from "../../pipelines.model";
import Workspace = Blockly.Workspace;

declare var Blockly: any;

@Component({
    selector: "blockly-workspace",
    template: `
        <div class="ml-2">
            <div class="row" style="min-height:600px">

                <div id="blocklyDiv" class="col-8 p-0"></div>

                <filter-information [selectedFilter]="selectedFilter$ | async" id="code"
                                    class="col-4"></filter-information>

            </div>
            <div class="row mt-1">
                <div class="col-8 p-0 card">
                    <div class="d-flex justify-content-between card-footer">
                        <div>
                            <button class="btn btn-danger" (click)="deletePipeline()"><img
                                    src="/assets/images/ic_delete_white_24px.svg"
                                    alt="Delete"/> {{'GENERAL.DELETE' | translate}}
                            </button>
                        </div>
                        <div>
                            <button clasS="btn btn-secondary mr-1" (click)="cancelPipelineEditing()"><img
                                    src="/assets/images/ic_cancel_white_24px.svg" alt="Cancel"/>
                                {{'GENERAL.CANCEL' | translate}}
                            </button>
                            <button class="btn btn-success" (click)="savePipelineEditing()">
                                <img src="/assets/images/ic_save_white.svg" alt="Save"/> {{'GENERAL.SAVE' | translate}}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    `,

    providers: [
        ToolBarBuilderService
    ]
})

export class BlocklyComponent implements OnInit, OnDestroy {
    @Input() public pipeline: InternalPipelineConfiguration;
    @Input() public filterDescriptors$: Observable<FilterDescriptor[]>;
    @Input() public categories$: Observable<string[]>;
    @Input() public isLoading$: Observable<boolean>;

    @Output() public update: EventEmitter<PipelineConfiguration> = new EventEmitter();
    @Output() public remove: EventEmitter<string> = new EventEmitter();

    private workspace: Workspace = undefined;

    private blocklyDiv;
    private toolbox: any;
    private isAlive: boolean = true;
    private loadingObservable$: Observable<boolean>;

    private selectedFilter$: Observable<FilterDescriptor | InternalPipelineConfiguration>;
    private selectedBlockName$: BehaviorSubject<string> = new BehaviorSubject("pipeline_configuration");

    constructor(private toolbarBuilder: ToolBarBuilderService, private translate: TranslateService) {
    }

    public ngOnInit(): void {
        this.selectedFilter$ = combineLatest(this.selectedBlockName$, this.filterDescriptors$).pipe(
            map(([name, descriptors]) =>
                name === "pipeline_configuration" ? this.pipeline : descriptors.find((d) => d.name === name))
        );

        this.isLoading$.pipe(takeWhile(() => this.isAlive),
            filter((loading) => loading === false),
            delay(1)).subscribe((_) => {
            Blockly.svgResize(this.workspace);
            this.selectedBlockName$.next("pipeline_configuration");
        });

        this.initBlockly();
    }

    public ngOnDestroy() {
        this.isAlive = false;
    }

    public savePipelineEditing() {
        const pipelineConfiguration: PipelineConfiguration =
            JSON.parse(Blockly.JavaScript.workspaceToCode(this.workspace)) as PipelineConfiguration;
        pipelineConfiguration.id = this.pipeline.id;
        this.update.emit(pipelineConfiguration);
    }

    public deletePipeline() {
        this.remove.emit(this.pipeline.id);
    }

    private onWorkspaceChange(e: any) {
        if (e instanceof Blockly.Events.Create || e.element === "click") {
            this.updateSelectedBlock(e.block ? e.block.blockId : e.blockId);
        }
    }

    private initBlockly() {
        this.toolbarBuilder.createPipelineBlock();
        this.blocklyDiv = document.getElementById("blocklyDiv");

        combineLatest(this.filterDescriptors$, this.categories$)
            .pipe(takeWhile((_) => this.isAlive))
            .subscribe(([descriptors, categories]) => {
                let currentWorkspace;
                if (typeof this.workspace !== "undefined") {
                    currentWorkspace = Blockly.Xml.workspaceToDom(this.workspace);
                }
                this.toolbox = this.toolbarBuilder.createToolbar(descriptors, categories);
                const currentBlocklyDiv = document.getElementById("blocklyDiv");
                while (currentBlocklyDiv.firstChild) {
                    currentBlocklyDiv.removeChild(currentBlocklyDiv.firstChild);
                }
                this.workspace = Blockly.inject("blocklyDiv", {
                    toolbox: this.toolbox,
                    zoom:
                        {
                            controls: true,
                            wheel: true,
                            startScale: 1.0,
                            maxScale: 3,
                            minScale: 0.3,
                            scaleSpeed: 1.2
                        }
                });
                this.workspace.addChangeListener((e: any) => this.onWorkspaceChange(e));
                this.workspace.addChangeListener(Blockly.Events.disableOrphans);
                const pipelineBlockXml =
                    '<xml><block type="pipeline_configuration" deletable="false" movable="false"></block></xml>';
                Blockly.Xml.domToWorkspace(
                    typeof currentWorkspace === "undefined" ?
                        Blockly.Xml.textToDom(pipelineBlockXml) :
                        currentWorkspace, this.workspace);
                Blockly.svgResize(this.workspace);

            });
    }

    private updateSelectedBlock(blockId: string) {

        if (blockId != null) {
            const block = this.workspace.getBlockById(blockId);
            this.selectedBlockName$.next(block.type);
        }

    }

}
