import {Component, OnDestroy, OnInit} from '@angular/core';
import {Blockly} from "node-blockly/browser";


declare var Blockly: any;

@Component({
    selector: 'blockly-workspace',
    template: `
        <h3>BLOCKLY</h3>
        <div id="blocklyDiv" style="height: 480px; width: 600px;"></div>
    `,
    providers: []
})

export class BlocklyComponent implements OnInit {
    private _workspace: any;

    toolbox: any;
    name: string = '';
    generatedCode: string = '// generated code will appear here';

    constructor() {
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
    }
}
