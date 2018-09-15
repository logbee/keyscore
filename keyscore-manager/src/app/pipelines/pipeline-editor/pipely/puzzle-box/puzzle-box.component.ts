import {Component, Input, OnInit} from "@angular/core";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {Workspace} from "../models/contract";

@Component({
    selector: "puzzle-box",
    template: `
        <div fxLayout="column">

            <puzzle-category [workspace]="workspace" [descriptors]="descriptors" [category]="'TestCategory'">
            </puzzle-category>
            <mat-divider></mat-divider>
        </div>
    `
})

export class PuzzleBoxComponent implements OnInit {
    @Input() descriptors: BlockDescriptor[];
    @Input() workspace: Workspace;

    categorySeparatedDescriptors: Map<string, BlockDescriptor[]> = new Map();

    ngOnInit() {
        this.separateCategories();
    }

    private separateCategories() {
        this.descriptors.forEach(descriptor => {

        })
    }


}