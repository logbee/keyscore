import {Component, EventEmitter, Input, OnInit, Output, ViewChild, ViewContainerRef} from "@angular/core";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {Dropzone, Workspace} from "../models/contract";
import {DraggableFactory} from "../draggable/draggable-factory";
import {DropzoneFactory} from "../dropzone/dropzone-factory";
import {generateRef} from "@/../modules/keyscore-manager-models/src/main/common/Ref";
import {JSONCLASS_PARAMETERSET} from "@/../modules/keyscore-manager-models/src/main/common/Configuration";
import {ParameterFactoryService} from "@/../modules/keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {template} from "@angular-devkit/core";


@Component({
    selector: "puzzle-category",
    template: `
        <div class="category-container" fxLayout="row">
            <div fxFlex="10" fxLayoutAlign="start center" style="color:black">{{category}}:</div>
            <div fxFlex="90" class="category-block-container">
                <ng-template #blockContainer></ng-template>
            </div>
        </div>
        
    `
})
export class PuzzleCategoryComponent implements OnInit {

    @Input() category: string;

    @Input() set descriptors(val: BlockDescriptor[]) {
        this._descriptors = val;
        setTimeout(() => this.createDraggables());

    };

    get descriptors(): BlockDescriptor[] {
        return this._descriptors;
    }

    private _descriptors: BlockDescriptor[];

    @Input() workspace: Workspace;

    @Output() onDraggablesCreated: EventEmitter<string> = new EventEmitter();
    @Output() onInit:EventEmitter<void> = new EventEmitter();

    @ViewChild("blockContainer", {read: ViewContainerRef}) blockContainer: ViewContainerRef;

    private toolbarDropzone: Dropzone;

    constructor(private dropzoneFactory: DropzoneFactory, private draggableFactory: DraggableFactory, private parameterFactory: ParameterFactoryService) {
        this.onInit.emit();
    }

    ngOnInit() {
        this.toolbarDropzone = this.dropzoneFactory.createToolbarDropzone(this.blockContainer, this.workspace);
    }

    private createDraggables() {
        this.toolbarDropzone.getDraggableContainer().clear();
        this.descriptors.forEach(blockDescriptor => {
            if (blockDescriptor.ref.uuid === "bf9c0ff2-64d5-44ed-9957-8128a50ab567") {
                console.log("TextMUTATOR:::::", blockDescriptor);
            }
            let parameters = blockDescriptor.parameters.map(parameterDescriptor =>
                this.parameterFactory.parameterDescriptorToParameter(parameterDescriptor));

            let blockConfiguration = {
                ref: generateRef(),
                parent: null,
                parameterSet: {
                    jsonClass: JSONCLASS_PARAMETERSET,
                    parameters: parameters
                }
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
        this.onDraggablesCreated.emit(this.category);
    }
}
