import {Component, Input, OnChanges, OnInit} from "@angular/core";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {Workspace} from "../models/contract";
import {FormControl} from "@angular/forms";
import {Observable, Subject} from "rxjs";
import {debounceTime, map, startWith, takeUntil, tap} from "rxjs/operators";
import * as _ from 'lodash';

@Component({
    selector: "puzzle-box",
    template: `
        <div fxLayout="column">
            <div class="puzzle-search-field mat-elevation-z2">
                <mat-form-field>
                    <input #searchInput matInput placeholder="search" [formControl]="searchFormControl"/>
                    <button mat-button *ngIf="searchInput.value" matSuffix mat-icon-button aria-label="Clear"
                            (click)="searchFormControl.setValue('')">
                        <mat-icon>close</mat-icon>
                    </button>
                    <button mat-button mat-icon-button matPrefix disabled>
                        <mat-icon>search</mat-icon>
                    </button>
                </mat-form-field>
            </div>
            <div fxLayout="column" class="category-wrapper" fxLayoutAlign="start">
                <div class="overlay" *ngIf="isLoading">
                    <div class="spinner-wrapper">
                        <mat-progress-spinner [diameter]="75" mode="indeterminate">
                        </mat-progress-spinner>
                    </div>
                </div>
                <div *ngIf="this.categories.length">
                    <ng-container *ngFor="let category of categories">
                        <puzzle-category [workspace]="workspace"
                                         [descriptors]="categorySeparatedDescriptors.get(category)"
                                         [category]="category" (onDraggablesCreated)="onDraggablesCreated($event)">
                        </puzzle-category>
                        <mat-divider></mat-divider>
                    </ng-container>
                </div>
                <div *ngIf="!this.categories.length" class="no-results-wrapper" fxLayoutAlign="center">
                    <span translate>GENERAL.NO_RESULTS</span>
                </div>
            </div>
        </div>


    `,
    styleUrls: ['./puzzle-box.component.scss']
})

export class PuzzleBoxComponent implements OnChanges, OnInit {
    @Input() descriptors: BlockDescriptor[];
    @Input() workspace: Workspace;

    categorySeparatedDescriptors: Map<string, BlockDescriptor[]> = new Map();
    categories: string[] = [];
    searchFormControl = new FormControl();

    filteredDescriptors$: Observable<BlockDescriptor[]>;
    unsubscribe$: Subject<void> = new Subject();
    isLoading: boolean = true;

    private _loadedCategories: string[] = [];

    onDraggablesCreated(cat: string) {
        this._loadedCategories.push(cat);
        if (this._loadedCategories.length === this.categories.length) {
            this.isLoading = false;
        }
    }


    ngOnInit() {
        this.filteredDescriptors$ = this.searchFormControl.valueChanges.pipe(
            debounceTime(300),
            tap(() => {
                this._loadedCategories = [];
                this.isLoading = true;
            }),
            startWith(''),
            map(val => this.filter(val)),
            tap(res => {
                if (!res.length) {
                    this.isLoading = false;
                }
            }));

        this.filteredDescriptors$.pipe(takeUntil(this.unsubscribe$)).subscribe(descriptors => {
            this.separateCategories(descriptors);
        })
    }

    ngOnChanges(changes) {
        if (changes['descriptors']) {
            this.separateCategories(this.descriptors)
        }
    }

    private filter(value: string): BlockDescriptor[] {
        if (!this.descriptors || !this.descriptors.length) return [];
        if (!value) return this.descriptors;
        let filteredDescriptors = _.cloneDeep(this.descriptors);
        let filterValue = value.toLowerCase();

        return filteredDescriptors.filter(descriptor =>
            descriptor.displayName.toLowerCase().includes(filterValue) ||
            descriptor.categories.filter(cat => cat.displayName.toLowerCase().includes(filterValue)).length)

    }

    private separateCategories(descriptors: BlockDescriptor[]) {
        this.categorySeparatedDescriptors = new Map();
        this.categories = descriptors.map(descriptor => descriptor.categories.map(cat => cat.displayName))
            .reduce((acc, val) => acc.concat(val), []).filter((category, i, array) => array.indexOf(category) === i);
        this.categories.forEach(category =>
            this.categorySeparatedDescriptors.set(category, descriptors.filter(descriptor => descriptor.categories.map(cat => cat.displayName).includes(category))));

    }

    getKeys(map: Map<any, any>) {
        return Array.from(map.keys());
    }


}