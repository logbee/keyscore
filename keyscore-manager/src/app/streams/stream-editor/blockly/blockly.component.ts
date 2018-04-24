import {Component, OnDestroy, OnInit} from '@angular/core';
import {Blockly} from "node-blockly/browser";

import {Store} from "@ngrx/store";
import {FilterDescriptor, getFilterCategories, getFilterDescriptors, StreamsState} from "../../streams.model";
import {Observable} from "rxjs/Observable";
import {ToolBarBuilderService} from "../../../services/blockly/toolbarbuilder.service";
import {LoadFilterDescriptorsAction} from "../../streams.actions";


declare var Blockly: any;

@Component({
    selector: 'blockly-workspace',
    template: `
        <div class="row" style="min-height:600px">
            <div id="blocklyArea" class="col-9" style="position:relative" >
                <div id="blocklyDiv" style="position: absolute"></div>
            </div>
            <textarea id="code" class="col-2 ml-3" [(ngModel)]="generatedCode"></textarea>
        </div>
        
    `,

    providers: [
        ToolBarBuilderService
    ]
})

export class BlocklyComponent implements OnInit {
    private _workspace: any;
    private categories$: Observable<string[]>;
    private filterDescriptors$: Observable<FilterDescriptor[]>;
    private toolbox: any;

    private blocklyDiv;
    private blocklyArea;

    generatedCode = "Here goes the code";

    constructor(private store: Store<StreamsState>, private toolbarBuilder: ToolBarBuilderService) {
        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.categories$ = this.store.select(getFilterCategories);

        this.store.dispatch(new LoadFilterDescriptorsAction());
    }

    ngOnInit(): void {
        console.log(Blockly);
        this.blocklyDiv = document.getElementById('blocklyDiv');
        this.blocklyArea = document.getElementById('blocklyArea');
        this.filterDescriptors$.combineLatest(this.categories$).subscribe(([descriptors, categories]) => {
            this.toolbox = this.toolbarBuilder.createToolbar(descriptors, categories);
            let currentBlocklyDiv = document.getElementById('blocklyDiv');
            while (currentBlocklyDiv.firstChild) {
                currentBlocklyDiv.removeChild(currentBlocklyDiv.firstChild);
            }
            this._workspace = Blockly.inject('blocklyDiv', {toolbox: this.toolbox});
            this._workspace.addChangeListener((e: any) => this.onWorkspaceChange(e));
        });

        window.addEventListener('resize', this.onResize, false);
        this.onResize(null);
        Blockly.svgResize(this._workspace);
    }

    onWorkspaceChange(e: any) {
        console.log(e);
        this.generatedCode = Blockly.JavaScript.workspaceToCode(this._workspace);
    }

    onResize(e: any) {
        var element = this.blocklyArea;

        // Position blocklyDiv over blocklyArea.
        this.blocklyDiv.style.left = element.offsetLeft + 'px';
        this.blocklyDiv.style.top = element.offsetTop + 'px';
        this.blocklyDiv.style.width = this.blocklyArea.offsetWidth + 'px';
        this.blocklyDiv.style.height = this.blocklyArea.offsetHeight + 'px';

    }
}
