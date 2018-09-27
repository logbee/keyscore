import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {Store} from "@ngrx/store";

import "../style/global-table-styles.css";
import {MatPaginator, MatSort} from "@angular/material";
import {selectBlueprints} from "./resources.reducer";
import {BlueprintDataSource} from "../dataSources/BlueprintDataSource";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {Blueprint} from "../models/blueprints/Blueprint";
import {StoreConfigurationRefAction, StoreDescriptorRefAction} from "./resources.actions";

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
                    <td mat-cell *matCellDef="let element">
                        <status-light align="left" [status]="Unknown"></status-light>
                    </td>
                </ng-container>

                <!--Resource Id Column-->
                <ng-container matColumnDef="uuid">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Resource Id</th>
                    <td mat-cell *matCellDef="let blueprint">{{blueprint?.ref.uuid}}</td>
                </ng-container>




                <ng-container matColumnDef="test">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>test</th>
                    <td mat-cell *matCellDef="let blueprint">
                                        {{blueprint.descriptor.uuid}}
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
                    <td mat-cell *matCellDef="let blueprint" [attr.colspan]="3">
                        <json-visualizer
                                class="jsonViewer"
                                style="overflow: hidden; display: flex"
                                [class.visible]="expandedElement === blueprint">
                        </json-visualizer>
                    </td>
                </ng-container>

                <!--Defining header row -->
                <tr mat-header-row *matHeaderRowDef="['uuid', 'jsonClass', 'health']"></tr>

                <!--Defining row with uuid jsonClass and health columns-->
                <tr mat-row *matRowDef="let blueprint; columns: ['uuid', 'jsonClass', 'health']"
                    class="example-element-row"
                    [class.expanded]="expandedElement === blueprint"
                    (click)="storeIds(blueprint)"
                    (click)="setExpanded(blueprint)">
                </tr>

                <!--Defining collapasable rows -->
                <tr mat-row *matRowDef="let blueprint; columns: ['expandedDetail'];
                 when: isExpansionDetailRow"
                    class="example-detail-row"
                    [@detailExpand]="blueprint === expandedElement ? 'expanded' : 'collapsed'"
                    style="overflow: hidden">
                </tr>
            </table>
        </div>
    `
})

export class ResourcesComponent implements AfterViewInit {

    private title: string = "Resources Overview";
    dataSource: BlueprintDataSource = new BlueprintDataSource(this.store.select(selectBlueprints));
    isExpansionDetailRow = (i: number) => i % 2 === 1;
    expandedElement: any;

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    constructor(private store: Store<any>) {

    }

    storeIds(blueprint: Blueprint) {
        console.log("storeIds Method body reached");
        this.store.dispatch(new StoreDescriptorRefAction(blueprint.descriptor.uuid));
        this.store.dispatch(new StoreConfigurationRefAction(blueprint.configuration.uuid));
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
        this.expandedElement = blueprint
    }


}