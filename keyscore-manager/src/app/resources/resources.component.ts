import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {Store} from "@ngrx/store";

import "../resources/resources-styles.css";
import {MatPaginator, MatSort} from "@angular/material";
import {selectBlueprints} from "./resources.reducer";
import {BlueprintDataSource} from "../dataSources/BlueprintDataSource";
import {animate, state, style, transition, trigger} from "@angular/animations";

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
                <mat-form-field fxFlex class="search-position">
                    <input matInput (keyup)="applyFilter($event.target.value)"
                           placeholder="{{'GENERAL.FILTER' | translate}}">
                </mat-form-field>
            <table fxFlex="95" #table mat-table matSort [dataSource]="dataSource"
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
                    </td>
                </ng-container>

                <!--Content for Expanded-->
                <ng-container matColumnDef="expandedDetail">
                    <mat-cell *matCellDef="let blueprint">
                        Descriptor {{blueprint.descriptor.uuid}}
                    </mat-cell>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="['uuid', 'jsonClass', 'health'];"></tr>

                <tr mat-row *matRowDef="let blueprint; columns: ['uuid', 'jsonClass', 'health'];"
                    (click)="setExpanded(blueprint)">
                </tr>

                <tr mat-row *matRowDef="let blueprint; columns: ['expandedDetail'];
                 when: isExpansionDetailRow"
                    [@detailExpand]="blueprint === expandedElement ? 'expanded' : 'collapsed'"
                    style="overflow: hidden">
                </tr>

                <!--<tr mat-row *matRowDef="let row; columns: displayedColumns;" (click) ="rowClicked(row)"></tr>-->
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

    setExpanded(row: any) {
        this.expandedElement = row
    }
}