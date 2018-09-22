import {Component, Input, OnInit} from "@angular/core";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {Workspace} from "../models/contract";
import {Observable} from "rxjs";

@Component({
    selector: "puzzle-box",
    template: `
        <div fxLayout="column">

            <ng-container *ngFor="let category of categories">
                <puzzle-category [workspace]="workspace" [descriptors]="categorySeparatedDescriptors.get(category)"
                                 [category]="category">
                </puzzle-category>
                <mat-divider></mat-divider>
            </ng-container>
        </div>
    `
})

export class PuzzleBoxComponent implements OnInit {
    @Input() descriptors$: Observable<BlockDescriptor[]>;
    @Input() workspace: Workspace;

    descriptors: BlockDescriptor[];
    categorySeparatedDescriptors: Map<string, BlockDescriptor[]> = new Map();
    categories: string[] = [];

    ngOnInit() {
        this.descriptors$.subscribe(descriptors => {
            this.descriptors = descriptors;
            this.separateCategories();
        });
    }

    private separateCategories() {
        this.categories = this.descriptors.map(descriptor => descriptor.categories)
            .reduce((acc, val) => acc.concat(val), []).filter((category, i, array) => array.indexOf(category) === i);
        this.categories.forEach(category =>
            this.categorySeparatedDescriptors.set(category, this.descriptors.filter(descriptor => descriptor.categories.includes(category))));

    }

    getKeys(map: Map<any, any>) {
        return Array.from(map.keys());
    }


}