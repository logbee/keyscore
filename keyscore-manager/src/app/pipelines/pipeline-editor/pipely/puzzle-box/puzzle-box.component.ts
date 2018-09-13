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
            <puzzle-category [workspace]="workspace" [descriptors]="descriptors" [category]="'TestCategory2'">

            </puzzle-category>
            <mat-divider></mat-divider>
            <puzzle-category [workspace]="workspace" [descriptors]="descriptors" [category]="'TestCategory3'">

            </puzzle-category>
            <mat-divider></mat-divider>
            <puzzle-category [workspace]="workspace" [descriptors]="descriptors" [category]="'TestCategory'">

            </puzzle-category>
            <mat-divider></mat-divider>
            <puzzle-category [workspace]="workspace" [descriptors]="descriptors" [category]="'TestCategory2'">

            </puzzle-category>
            <mat-divider></mat-divider>
            <puzzle-category [workspace]="workspace" [descriptors]="descriptors" [category]="'TestCategory3'">

            </puzzle-category>
            <mat-divider></mat-divider>
            <puzzle-category [workspace]="workspace" [descriptors]="descriptors" [category]="'TestCategory'">

            </puzzle-category>
            <mat-divider></mat-divider>
            <puzzle-category [workspace]="workspace" [descriptors]="descriptors" [category]="'TestCategory2'">

            </puzzle-category>
            <mat-divider></mat-divider>
            <puzzle-category [workspace]="workspace" [descriptors]="descriptors" [category]="'TestCategory3'">

            </puzzle-category>
        </div>
    `
})

export class PuzzleBoxComponent implements OnInit {
    @Input() descriptors: BlockDescriptor[];
    @Input() workspace: Workspace;

    ngOnInit() {

    }


}