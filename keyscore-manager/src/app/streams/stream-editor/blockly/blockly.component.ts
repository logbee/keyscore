import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Blockly} from "node-blockly/browser";
import {FilterDescriptor, StreamConfiguration, StreamModel} from "../../streams.model";
import {combineLatest, Observable, ReplaySubject} from "rxjs";
import {ToolBarBuilderService} from "../../../services/blockly/toolbarbuilder.service";
import {map, startWith} from "rxjs/internal/operators";
import {TranslateService} from "@ngx-translate/core";
import Workspace = Blockly.Workspace;


declare var Blockly: any;

@Component({
    selector: 'blockly-workspace',
    template: `
        <div class="row pl-1" style="min-height:600px">

            <div id="blocklyArea" class="col-8 pr-2" style="position:relative">
                <div id="blocklyDiv" style="position: absolute"></div>
            </div>

            <div id="code" class="col-4">
                <div class="card">
                    <div class="card-header" style="font-size: 22px">{{(selectedFilter$ | async).displayName}}</div>
                    <div class="card-body">{{(selectedFilter$ | async).description}}</div>
                    <div class="card-footer"></div>
                </div>
            </div>

        </div>
        <div class="row pl-2 mt-1">
            <div class="col-8 card p-0">
                <div class="d-flex justify-content-between card-footer">
                    <div>
                        <button class="btn btn-danger" (click)="deleteStream()"><img
                                src="/assets/images/ic_delete_white_24px.svg"
                                alt="Delete"/> {{'GENERAL.DELETE' | translate}}</button>
                    </div>
                    <div>
                        <button clasS="btn btn-secondary mr-1" (click)="cancelStreamEditing()"><img
                                src="/assets/images/ic_cancel_white_24px.svg" alt="Cancel"/> {{'GENERAL.CANCEL' | translate}}</button>
                        <button class="btn btn-success" (click)="saveStreamEditing()"><img src="/assets/images/ic_save_white.svg" alt="Save"/> {{'GENERAL.SAVE' | translate}}
                        </button>
                    </div>
                </div>
            </div>
        </div>

    `,

    providers: [
        ToolBarBuilderService
    ]
})

export class BlocklyComponent implements OnInit {
    @Input() stream: StreamModel;
    @Input() filterDescriptors$: Observable<FilterDescriptor[]>;
    @Input() categories$: Observable<string[]>;

    @Output() update: EventEmitter<StreamModel> = new EventEmitter();
    @Output() remove: EventEmitter<StreamModel> = new EventEmitter();


    private workspace: Workspace = undefined;

    private blocklyDiv;
    private blocklyArea;
    private toolbox: any;

    private selectedFilter$: Observable<FilterDescriptor>;
    private selectedBlockName$: ReplaySubject<string> = new ReplaySubject();


    constructor(private toolbarBuilder: ToolBarBuilderService, private translate: TranslateService) {

    }

    ngOnInit(): void {
        console.log(Blockly);
        this.initBlockly();

        this.selectedFilter$ = combineLatest(this.selectedBlockName$, this.filterDescriptors$).pipe(
            map(([name, descriptors]) => descriptors.filter(d => d.name === name)[0]),
            startWith({
                name: "StartDummy",
                displayName: "Stream configuration",
                description: "Choose a filter from the toolbox to get started!",
                previousConnection: null,
                nextConnection: null,
                parameters: [],
                category: null
            }));


    }

    saveStreamEditing(){
        let streamConfiguration:StreamConfiguration = JSON.parse(Blockly.JavaScript.workspaceToCode(this.workspace)) as StreamConfiguration;
        streamConfiguration.id = this.stream.id;
        console.log(JSON.stringify(streamConfiguration));
    }

    deleteStream(){
        this.remove.emit(this.stream);
    }


    private onWorkspaceChange(e: any) {
        console.log(e);
        if (e.element === 'selected') {
            this.updateSelectedBlock(e.newValue);
        }
    }

    private onResize(e: any) {
        var element = this.blocklyArea;

        // Position blocklyDiv over blocklyArea.
        this.blocklyDiv.style.left = element.offsetLeft + 'px';
        this.blocklyDiv.style.top = element.offsetTop + 'px';
        this.blocklyDiv.style.width = this.blocklyArea.offsetWidth + 'px';
        this.blocklyDiv.style.height = this.blocklyArea.offsetHeight + 'px';

    }

    private initBlockly() {
        this.blocklyDiv = document.getElementById('blocklyDiv');
        this.blocklyArea = document.getElementById('blocklyArea');
        combineLatest(this.filterDescriptors$, this.categories$).subscribe(([descriptors, categories]) => {
            let currentWorkspace = undefined;
            if (typeof this.workspace != "undefined"){
                currentWorkspace = Blockly.Xml.workspaceToDom(this.workspace);
            }
            this.toolbox = this.toolbarBuilder.createToolbar(descriptors, categories);
            let currentBlocklyDiv = document.getElementById('blocklyDiv');
            while (currentBlocklyDiv.firstChild) {
                currentBlocklyDiv.removeChild(currentBlocklyDiv.firstChild);
            }
            this.workspace = Blockly.inject('blocklyDiv', {
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
            this.toolbarBuilder.createStreamBlock();
            let streamBlockXml = '<xml><block type="stream_configuration" deletable="false" movable="false"></block></xml>';

            Blockly.Xml.domToWorkspace(typeof currentWorkspace === "undefined" ? Blockly.Xml.textToDom( streamBlockXml) : currentWorkspace, this.workspace);

        });
        this.workspace.addChangeListener(Blockly.Events.disableOrphans);
        window.addEventListener('resize', this.onResize, false);
        this.onResize(null);
        Blockly.svgResize(this.workspace);


    }

    private updateSelectedBlock(blockId: string) {
        if (blockId != null) {
            let block = this.workspace.getBlockById(blockId);
            if (block.type != 'stream_configuration') this.selectedBlockName$.next(block.type);
        }

    }




}
