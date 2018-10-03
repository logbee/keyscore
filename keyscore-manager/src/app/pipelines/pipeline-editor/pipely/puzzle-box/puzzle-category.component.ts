import {Component, Input, OnInit, ViewChild, ViewContainerRef} from "@angular/core";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {Dropzone, Workspace} from "../models/contract";
import {DraggableFactory} from "../draggable/draggable-factory";
import {DropzoneFactory} from "../dropzone/dropzone-factory";
import {parameterDescriptorToParameter} from "../../../../util";
import {v4 as uuid} from "uuid";
import {generateRef} from "../../../../models/common/Ref";
import {Category} from "../../../../models/descriptors/Category";


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
            let parameters = blockDescriptor.parameters.map(parameterDescriptor =>
                parameterDescriptorToParameter(parameterDescriptor));

            let blockConfiguration = {
                ref: generateRef(),
                descriptor: blockDescriptor,
                parameters: parameters
            };
            this.draggableFactory.createDraggable(this.toolbarDropzone.getDraggableContainer(), {
                blockDescriptor: blockDescriptor,
                blockConfiguration: blockConfiguration,
                blueprintRef: generateRef(),
                initialDropzone: this.toolbarDropzone,
                next: null,
                color: this.computeColor(blockDescriptor.categories.map(cat => cat.name)),
                previous: null,
                rootDropzone: this.toolbarDropzone.getDropzoneModel().dropzoneType,
                isMirror: false
            }, this.workspace);
        });
    }

    private computeColor(categories: string[]): string {
        const colors: string[] = ['#cc0000', '#e69138', '#f1c232', '#6aa84f', '#45818e', '#3d85c6', '#674ea7'];
        let categoryHash = this.categoryHashCode(categories.reduce((acc, category) => acc + category, ""));
        return colors[Math.abs(categoryHash % colors.length)];
    }

    private categoryHashCode(cats: string): number {
        let hash = 0, i, chr;
        if (cats.length === 0) return hash;
        for (i = 0; i < cats.length; i++) {
            chr = cats.charCodeAt(i);
            hash = ((hash << 5) - hash) + chr;
            hash |= 0; // Convert to 32bit integer
        }
        return hash;
    }

    private getRandomInt(min: number, max: number): number {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }
}