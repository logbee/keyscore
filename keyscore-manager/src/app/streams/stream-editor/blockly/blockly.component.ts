import {Component, OnDestroy, OnInit} from '@angular/core';
import {Blockly} from "node-blockly/browser";

import {Store} from "@ngrx/store";
import {FilterDescriptor, getFilterCategories, getFilterDescriptors, StreamsState} from "../../streams.model";
import {Observable} from "rxjs/Observable";
import {ToolBarBuilderService} from "../../../services/blockly/toolbarbuilder.service";
import {LoadFilterDescriptorsAction} from "../../streams.actions";
import {ReplaySubject} from "rxjs/ReplaySubject";


declare var Blockly: any;

@Component({
    selector: 'blockly-workspace',
    template: `
        <div class="row" style="min-height:600px">
            <div id="blocklyArea" class="col-9" style="position:relative">
                <div id="blocklyDiv" style="position: absolute"></div>
            </div>
            <div id="code" class="col-2 ml-3">
                <div class="col-12" style="font-size: 22px">{{(currentBlock$ | async).name}}</div>
                <div class="col-12">{{(currentBlock$ | async).description}}</div>
            </div>
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

    private currentXml: XMLDocument;
    private blocklyDiv;
    private blocklyArea;

    private selectedBlock$: ReplaySubject<String> = new ReplaySubject();
    private currentBlock$: ReplaySubject<{ name: string, description: string }> = new ReplaySubject();

    constructor(private store: Store<StreamsState>, private toolbarBuilder: ToolBarBuilderService) {
        this.filterDescriptors$ = this.store.select(getFilterDescriptors);
        this.categories$ = this.store.select(getFilterCategories);
        this.currentBlock$.next({name:'Test',description:'Beispiel Beschreibung'});
        this.selectedBlock$.next('');

        this.store.dispatch(new LoadFilterDescriptorsAction())

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

        this.selectedBlock$.combineLatest(this.filterDescriptors$).subscribe(([blockName,descriptors]) =>{
            descriptors.filter(descriptor => descriptor.name === blockName).forEach(d => this.currentBlock$.next({name:d.displayName,description:d.description}));
        })

        window.addEventListener('resize', this.onResize, false);
        this.onResize(null);
        Blockly.svgResize(this._workspace);
    }

    onWorkspaceChange(e: any) {
        console.log(e);
        this.currentXml = Blockly.Xml.workspaceToDom(this._workspace);
        console.log(this.currentXml);
        if(e.element === 'selected') {
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
    private updateSelectedBlock(id: string) {
        let allBlocks = this.currentXml.getElementsByTagName("block");
        for(let i=0;i<allBlocks.length;i++){
            if(allBlocks[i].getAttribute('id') === id){
                this.selectedBlock$.next(allBlocks[i].getAttribute('type'));
                break;
            }
        }

    }
}
