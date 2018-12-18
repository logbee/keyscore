import {Component, Input, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {Dropzone, Workspace} from "../models/contract";
import {DraggableFactory} from "../draggable/draggable-factory";
import {DropzoneFactory} from "../dropzone/dropzone-factory";
import {parameterDescriptorToParameter} from "../../../../util";
import {v4 as uuid} from "uuid";
import {generateRef} from "../../../../models/common/Ref";
import {Category} from "../../../../models/descriptors/Category";
import {ParameterJsonClass} from "../../../../models/parameters/Parameter";


@Component({
    selector: "puzzle-category",
    template: `
        <div class="category-container" fxLayout="row">
            <div fxFlex="10" fxLayoutAlign="start center" style="color:black">{{category}}:</div>
            <div fxFlex="90" class="category-block-container">
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


    constructor(private dropzoneFactory: DropzoneFactory, private draggableFactory: DraggableFactory) {

    }

    ngOnInit() {
        this.toolbarDropzone = this.dropzoneFactory.createToolbarDropzone(this.blockContainer, this.workspace);
        this.createDraggables();
    }

    private createDraggables() {
        this.descriptors.forEach(blockDescriptor => {
            if(blockDescriptor.ref.uuid === "bf9c0ff2-64d5-44ed-9957-8128a50ab567"){
                console.log("TextMUTATOR:::::", blockDescriptor);
            }
            let parameters = blockDescriptor.parameters.map(parameterDescriptor =>
                parameterDescriptorToParameter(parameterDescriptor));

            let blockConfiguration = {
                ref: generateRef(),
                parent: null,
                parameterSet: {jsonClass: ParameterJsonClass.ParameterSet, parameters: parameters}
            };
            this.draggableFactory.createDraggable(this.toolbarDropzone.getDraggableContainer(), {
                blockDescriptor: blockDescriptor,
                configuration: blockConfiguration,
                blueprintRef: generateRef(),
                initialDropzone: this.toolbarDropzone,
                next: null,
                previous: null,
                rootDropzone: this.toolbarDropzone.getDropzoneModel().dropzoneType,
                isMirror: false,
                isSelected: false,
            }, this.workspace);
        });
    }

}