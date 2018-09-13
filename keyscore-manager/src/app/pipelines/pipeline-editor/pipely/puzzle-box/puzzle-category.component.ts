import {Component, Input, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {Dropzone, Workspace} from "../models/contract";
import {DraggableFactory} from "../draggable/draggable-factory";
import {DropzoneFactory} from "../dropzone/dropzone-factory";
import {parameterDescriptorToParameter} from "../../../../util";
import {v4 as uuid} from "uuid";


@Component({
    selector: "puzzle-category",
    template: `
        <div class="category-container" fxLayout="row">
            <div fxFlex="10" fxLayoutAlign="start center" style="color:black">{{category}}:</div>
            <div fxFlex="80" class="category-block-container" >
                <ng-template #blockContainer></ng-template>
            </div>
        </div>`
})

export class PuzzleCategoryComponent implements OnInit {
    @Input() category: string;
    @Input() descriptors: BlockDescriptor[];
    @Input() workspace: Workspace;

    @ViewChild("blockContainer", {read: ViewContainerRef}) blockContainer: ViewContainerRef;
    private toolbarDropzone: Dropzone;

    private exampleColor:string[] = ['#cc0000','#e69138','#f1c232','#6aa84f','#45818e','#3d85c6','#674ea7'];

    constructor(private dropzoneFactory: DropzoneFactory, private draggableFactory: DraggableFactory) {

    }

    ngOnInit() {
        this.toolbarDropzone = this.dropzoneFactory.createToolbarDropzone(this.blockContainer, this.workspace);
        this.createDraggables();
    }

    private createDraggables() {
        this.descriptors.forEach(blockDescriptor => {
            let parameters = blockDescriptor.parameters.map(parameterDescriptor =>
                parameterDescriptorToParameter(parameterDescriptor));
            let blockConfiguration = {
                id: uuid(),
                descriptor: blockDescriptor,
                parameters: parameters
            };
            this.draggableFactory.createDraggable(this.toolbarDropzone.getDraggableContainer(), {
                blockDescriptor: blockDescriptor,
                blockConfiguration: blockConfiguration,
                initialDropzone: this.toolbarDropzone,
                next: null,
                color:this.exampleColor[this.getRandomInt(0,6)],
                previous: null,
                rootDropzone: this.toolbarDropzone.getDropzoneModel().dropzoneType,
                isMirror: false
            }, this.workspace);
        })
    }

    private getRandomInt(min:number, max:number):number {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }
}