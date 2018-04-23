import {Component, OnDestroy, OnInit} from '@angular/core';
import {Blockly} from "node-blockly/browser";

import {Store} from "@ngrx/store";
import {FilterDescriptor, getFilterCategories, getFilterDescriptors, StreamsState} from "../../streams.model";
import {Observable} from "rxjs/Observable";


declare var Blockly: any;

@Component({
    selector: 'blockly-workspace',
    template: `
        
        <div id="blocklyDiv" style="height: 480px; width: 600px;"></div>
        <textarea id="code" [(ngModel)]="generatedCode"></textarea>
    `,
    providers: []
})

export class BlocklyComponent implements OnInit {
    private _workspace: any;
    private categories$:Observable<string[]>;
    private filterDescriptors$:Observable<FilterDescriptor[]>;
    private toolbox: any;

    generatedCode="Here goes the code";

    constructor(private store:Store<StreamsState>) {
        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.categories$ = this.store.select(getFilterCategories);
    }

    ngOnInit(): void {
        console.log(Blockly);

        this.toolbox =
            `<xml xmlns="http://www.w3.org/1999/xhtml" id="toolbox" style="display: none;">
                <block type="controls_if"></block>
                <block type="controls_whileUntil"></block>
            </xml>`;
        this._workspace = Blockly.inject('blocklyDiv', {toolbox:this.toolbox});
        this._workspace.addChangeListener((e: any) => this.onWorkspaceChange(e));
    }

    onWorkspaceChange(e: any) {
        console.log(e);
        this.generatedCode = Blockly.JavaScript.workspaceToCode(this._workspace);
    }
}
