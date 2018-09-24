import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {Go} from "../router/router.actions";
import {LoadAgentsAction} from "./agents.actions";
import {AgentModel, AgentsState, getAgents} from "./agents.model";
import {MatPaginator, MatSort, MatTableDataSource} from "@angular/material";
import {Blueprint} from "../models/blueprints/Blueprint";

@Component({
    selector: "keyscore-agents",
    template: `
        <header-bar
            [title]="title"
            [showManualReload]="true"
            (onManualReload)="reload()">
        </header-bar>
        <div fxFlexFill="" fxLayout="column" fxLayoutGap="15px" class="table-wrapper">
            <mat-form-field fxFlex="5%" class="search-position">
                <input matInput (keyup)="applyFilter($event.target.value)"
                       placeholder="{{'GENERAL.FILTER' | translate}}">
            </mat-form-field>

            <table fxFlex="95%" mat-table matSort [dataSource]="dataSource"
                   class="mat-elevation-z8 table-position">

                <ng-container matColumnDef="number">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>#</th>
                    <td mat-cell *matCellDef="let number = index">
                        {{number}}
                    </td>
                </ng-container>

                <ng-container matColumnDef="name">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Name</th>
                    <td mat-cell *matCellDef="let element">{{element?.name}}</td>
                </ng-container>

                <ng-container matColumnDef="host">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Host</th>
                    <td mat-cell *matCellDef="let element">{{element?.host}}</td>
                </ng-container>

                <ng-container matColumnDef="id">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Id</th>
                    <td mat-cell *matCellDef="let element">
                        {{element.id}}
                    </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;"(click) ="inspect(row.id)"></tr>

            </table>
            <mat-paginator [pageSizeOptions]="[5, 10, 25, 100]" showFirstLastButtons ></mat-paginator>
        </div>
    `
})

export class AgentsComponent implements AfterViewInit {
    private title: string = "Agents";
    private agents$: Observable<AgentModel[]>;
    displayedColumns: string[] = ['number', 'id', 'name', 'host'];
    dataSource: MatTableDataSource<any> = new MatTableDataSource([]);

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    constructor(private store: Store<AgentsState>) {
        this.agents$ = this.store.select(getAgents);
        this.agents$.subscribe(agents => {
            this.dataSource.data = agents;
        });
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

    public reload() {
        this.store.dispatch(new LoadAgentsAction());
    }

    public inspect(id: string) {
        this.store.dispatch(new Go({path: ["/agent/" + id + "/"]}));
    }
}
