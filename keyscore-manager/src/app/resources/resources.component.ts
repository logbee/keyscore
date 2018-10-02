import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {select, Store} from "@ngrx/store";
import "../style/style.css";
import "../style/global-table-styles.css";
import {MatPaginator, MatSort} from "@angular/material";
import {selectBlueprints, selectStateObjects} from "./resources.reducer";
import {BlueprintDataSource} from "../dataSources/BlueprintDataSource";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {Blueprint} from "../models/blueprints/Blueprint";
import {
    GetResourceStateAction,
    StoreBlueprintRefAction,
    StoreConfigurationRefAction,
    StoreDescriptorRefAction
} from "./resources.actions";
import {Go} from "../router/router.actions";
import {Observable} from "rxjs/index";
import {StateObject} from "../models/common/StateObject";
import {Health} from "../models/common/Health";

@Component({
    selector: "resource-viewer",
    animations: [
        trigger('detailExpand', [
            state('collapsed', style({height: '0px', minHeight: '0', visibility: 'hidden'})),
            state('expanded', style({height: '*', visibility: 'visible'})),
            transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
        ]),
    ],
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="title">
        </header-bar>
        <div fxFlexFill fxLayout="column" fxLayoutGap="15px" class="table-wrapper">
            <!--Search Field-->
            <mat-form-field fxFlex class="search-position">
                <input matInput (keyup)="applyFilter($event.target.value)"
                       placeholder="{{'GENERAL.FILTER' | translate}}">
            </mat-form-field>

            <!--Resources Table-->
            <table fxFlex="95" #table mat-table matSort [dataSource]="dataSource"
                   class="mat-elevation-z8 table-position">

                <!--Health Column-->
                <ng-container matColumnDef="health">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
                    <td mat-cell *matCellDef="let blueprint">
                        <resource-health [health]="determineHealthOfResource(blueprint)"></resource-health>
                    </td>
                </ng-container>


                <!--Link to Live-Editing-->
                <ng-container matColumnDef="link">
                    <th mat-header-cell *matHeaderCellDef>Live-Editing</th>
                    <td mat-cell *matCellDef="let blueprint">
                        <button mat-icon-button>
                            <mat-icon (click)="goToLiveEditing(blueprint)">link</mat-icon>
                        </button>
                    </td>
                </ng-container>

                <!--Resource Id Column-->
                <ng-container matColumnDef="uuid">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Resource Id</th>
                    <td mat-cell *matCellDef="let blueprint">{{blueprint?.ref.uuid}}</td>
                </ng-container>

                <!--Type Column-->
                <ng-container matColumnDef="jsonClass">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Type</th>
                    <td mat-cell *matCellDef="let blueprint">
                        <resource-type [type]="blueprint?.jsonClass"></resource-type>
                    </td>
                </ng-container>

                <!--Expandend Content Column-->
                <ng-container matColumnDef="expandedDetail">
                    <td mat-cell *matCellDef="let blueprint" [attr.colspan]="4">
                        <json-visualizer
                                class="jsonViewer"
                                [class.visible]="expandedElement === blueprint">
                        </json-visualizer>
                    </td>
                </ng-container>

                <!--Defining header row -->
                <tr mat-header-row *matHeaderRowDef="['health', 'jsonClass', 'uuid', 'link']"></tr>

                <!--Defining row with uuid jsonClass and health columns-->
                <tr mat-row *matRowDef="let blueprint; columns: ['health', 'jsonClass', 'uuid', 'link']"
                    class="example-element-row"
                    [class.expanded]="expandedElement === blueprint"
                    (click)="storeIds(blueprint)"
                    (click)="setExpanded(blueprint)">
                </tr>

                <!--Defining collapasable rows -->
                <tr mat-row *matRowDef="let blueprint; columns: ['expandedDetail'];
                 when: isExpansionDetailRow"
                    class="example-detail-row"
                    [@detailExpand]="blueprint === expandedElement ? 'expanded' : 'collapsed'">
                </tr>
            </table>
        </div>
    `
})

export class ResourcesComponent implements AfterViewInit, OnInit {

    private title: string = "Resources Overview";
    dataSource: BlueprintDataSource = new BlueprintDataSource(this.store.select(selectBlueprints));
    blueprints: Observable<Blueprint[]>;
    stateObjects$: Observable<StateObject[]>;
    isExpansionDetailRow = (i: number) => i % 2 === 1;
    expandedElement: any;
    stateObjects: StateObject[];

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    constructor(private store: Store<any>) {
        this.blueprints = this.store.select(selectBlueprints);
        this.blueprints.subscribe(blueprints => {
            blueprints.forEach((bp) => {
                this.store.dispatch(new GetResourceStateAction(bp.ref.uuid));
            })
        });
        this.stateObjects$ = this.store.pipe(select(selectStateObjects));
        this.stateObjects$.subscribe(objects => {
            this.stateObjects = objects;
        });
    }

    ngOnInit() {
    }

    determineHealthOfResource(blueprint: Blueprint) {
        const object = this.stateObjects.filter(elem => elem.resourceId == blueprint.ref.uuid)[0];
        if (object) {
            return object.resourceInstance.health;
        } else {
            return Health.Unknown;
        }
    }

    storeIds(blueprint: Blueprint) {
        this.store.dispatch(new StoreDescriptorRefAction(blueprint.descriptor.uuid));
        this.store.dispatch(new StoreConfigurationRefAction(blueprint.configuration.uuid));
        this.store.dispatch(new StoreBlueprintRefAction(blueprint.ref.uuid));
    }

    ngAfterViewInit() {
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
    }

    applyFilter(filterValue: string) {
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage()
        }
    }

    setExpanded(blueprint: any) {
        if (this.expandedElement == blueprint) {
            this.expandedElement = "";
        } else {
            this.expandedElement = blueprint;
        }

    }

    goToLiveEditing(blueprint: any) {
        this.store.dispatch(new Go({path: ["/filter/" + blueprint.ref.uuid]}))
    }

}