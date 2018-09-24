import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {Store} from "@ngrx/store";

import "../resources/resources-styles.css";
import {MatPaginator, MatSort} from "@angular/material";
import * as RouterActions from "../router/router.actions";
import {selectBlueprints} from "./resources.reducer";
import {BlueprintDataSource} from "../dataSources/BlueprintDataSource";

@Component({
    selector: "resource-viewer",
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="title">
        </header-bar>
        <div fxFlexFill fxLayout="column" fxLayoutGap="15px" class="table-wrapper">
            <mat-form-field fxFlex="5" class="search-position">
                <input matInput (keyup)="applyFilter($event.target.value)"
                       placeholder="{{'GENERAL.FILTER' | translate}}">
            </mat-form-field>

            <table fxFlex="95" mat-table matSort [dataSource]="dataSource"
                   class="mat-elevation-z8 table-position">

                <ng-container matColumnDef="health">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
                    <td mat-cell *matCellDef="let element">
                        <status-light align="left" [status]="Unknown"></status-light>
                    </td>
                </ng-container>
                
                <ng-container matColumnDef="uuid">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Resource Id</th>
                    <td mat-cell *matCellDef="let element">{{element?.ref.uuid}}</td>
                </ng-container>

                <ng-container matColumnDef="jsonClass">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Type</th>
                    <td mat-cell *matCellDef="let element">
                        <resource-type [type]="element?.jsonClass"></resource-type>
                        <!--{{element.jsonClass}}-->
                    </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;" (click) ="rowClicked(row)"></tr>
            </table>
            <mat-paginator [pageSizeOptions]="[5, 10, 25, 100]" showFirstLastButtons ></mat-paginator>
        </div>
    `
})

 export class ResourcesComponent implements AfterViewInit {

    displayedColumns: string[] = ['health', 'uuid', 'jsonClass'];
    private title: string = "Resources Overview";
    dataSource: BlueprintDataSource = new BlueprintDataSource(this.store.select(selectBlueprints));

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    constructor(private store: Store<any>) {
    }

    rowClicked(row: any) {
        let id = row.ref.uuid;
        this.store.dispatch(new RouterActions.Go({
            path: ["filter/" + id, {}],
            query: {},
            extras: {}
        }));
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
}