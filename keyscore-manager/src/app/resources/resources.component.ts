import {Component, OnInit} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs/index";
import {selectBlueprints} from "./resources.reducer";
import {Blueprint} from "../models/blueprints/Blueprint";

import "../resources/resources-styles.css";

@Component({
    selector: "resource-viewer",
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="title">
        </header-bar>
        <div fxFlexFill="" fxLayout="column" fxLayoutGap="15px" class="table-wrapper">
            <table fxFlex="95%" mat-table [dataSource]="blueprints$ | async" class="mat-elevation-z8 table-position">

                <ng-container matColumnDef="position">
                    <th mat-header-cell *matHeaderCellDef>No.</th>
                    <td mat-cell *matCellDef="let i = index;">{{i}}</td>
                </ng-container>
                
                <ng-container matColumnDef="id">
                    <th mat-header-cell *matHeaderCellDef>Resource Id</th>
                    <td mat-cell *matCellDef="let element">{{element.ref.uuid}}</td>
                </ng-container>

                <ng-container matColumnDef="type">
                    <th mat-header-cell *matHeaderCellDef>Type</th>
                    <td mat-cell *matCellDef="let element">{{element.jsonClass}}</td>
                </ng-container>
                
            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>
        </div>
    `
})
export class ResourcesComponent implements OnInit{
    displayedColumns: string[] = ['position', 'type', 'id'];
    private title: string = "Resources Overview";
    private blueprints$: Observable<Blueprint[]>;
    constructor(private store: Store<any>, private translate: TranslateService) {
    }

    ngOnInit(): void {
        this.blueprints$ = this.store.select(selectBlueprints);
    }

}