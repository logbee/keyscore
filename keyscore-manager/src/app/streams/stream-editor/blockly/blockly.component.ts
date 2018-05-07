import {Component, OnDestroy, OnInit} from '@angular/core';
import {Blockly} from "node-blockly/browser";

import {Store} from "@ngrx/store";
import {FilterDescriptor, getFilterCategories, getFilterDescriptors, StreamsState} from "../../streams.model";
import {combineLatest,Observable, ReplaySubject} from "rxjs";
import {ToolBarBuilderService} from "../../../services/blockly/toolbarbuilder.service";
import {LoadFilterDescriptorsAction} from "../../streams.actions";
import Workspace = Blockly.Workspace;
import {map, startWith} from "rxjs/internal/operators";


declare var Blockly: any;

@Component({
    selector: 'blockly-workspace',
    template: `
        <div class="row" style="min-height:600px">
            <div id="blocklyArea" class="col-8" style="position:relative">
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

    `,

    providers: [
        ToolBarBuilderService
    ]
})

export class BlocklyComponent implements OnInit {
    private workspace: Workspace;
    private categories$: Observable<string[]>;
    private filterDescriptors$: Observable<FilterDescriptor[]>;

    private blocklyDiv;
    private blocklyArea;
    private toolbox: any;

    private selectedFilter$: Observable<FilterDescriptor>;
    private selectedBlockName$: ReplaySubject<string> = new ReplaySubject();


    constructor(private store: Store<StreamsState>, private toolbarBuilder: ToolBarBuilderService) {
        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.categories$ = this.store.select(getFilterCategories);

        this.store.dispatch(new LoadFilterDescriptorsAction())

    }

    ngOnInit(): void {
        console.log(Blockly);
        this.initBlockly();

        this.selectedFilter$ = combineLatest(this.selectedBlockName$,this.filterDescriptors$).pipe(
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

    initBlockly() {
        this.blocklyDiv = document.getElementById('blocklyDiv');
        this.blocklyArea = document.getElementById('blocklyArea');
        combineLatest(this.filterDescriptors$,this.categories$).subscribe(([descriptors, categories]) => {
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

            Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom(streamBlockXml), this.workspace);

        });
        this.workspace.addChangeListener(Blockly.Events.disableOrphans);
        window.addEventListener('resize', this.onResize, false);
        this.onResize(null);
        Blockly.svgResize(this.workspace);


    }

    onWorkspaceChange(e: any) {
        console.log(e);
        if (e.element === 'selected') {
            this.updateSelectedBlock(e.newValue);
        }
    }

    onResize(e: any) {
        var element = this.blocklyArea;

        // Position blocklyDiv over blocklyArea.
        this.blocklyDiv.style.left = element.offsetLeft + 'px';
        this.blocklyDiv.style.top = element.offsetTop + 'px';
        this.blocklyDiv.style.width = this.blocklyArea.offsetWidth + 'px';
        this.blocklyDiv.style.height = this.blocklyArea.offsetHeight + 'px';

    }

    private updateSelectedBlock(blockId: string) {
        if (blockId != null) {
            let block = this.workspace.getBlockById(blockId);
            if (block.type != 'stream_configuration') this.selectedBlockName$.next(block.type);
        }

    }


}
