import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {Store} from "@ngrx/store";
import {LoadAgentsAction, RemoveCurrentAgentAction} from "./agents.actions";
import { MatPaginator } from "@angular/material/paginator";
import { MatSort } from "@angular/material/sort";
import { MatTable } from "@angular/material/table";
import {TranslateService} from "@ngx-translate/core";
import {AgentsState, getAgents} from "./agents.reducer";
import {AgentDataSource} from "../data-source/agent-data-source";

@Component({
    selector: "keyscore-agents",
    template: `
        <header-bar
                [title]="title"
                [showManualReload]="true"
                (onManualReload)="reload()">
        </header-bar>
        <div fxLayout="column" fxLayoutGap="15px" class="table-wrapper">
            <mat-form-field fxFlex="5%" class="search-position">
                <input matInput (keyup)="applyFilter($event.target.value)"
                       placeholder="{{'GENERAL.SEARCH' | translate}}">
                <button mat-button matSuffix mat-icon-button aria-label="Search">
                    <mat-icon>search</mat-icon>
                </button>
            </mat-form-field>

            <table fxFlex="90%" mat-table matSort [dataSource]="dataSource"
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
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
                    <td mat-cell *matCellDef="let element">
                        {{element.id}}
                    </td>
                </ng-container>

                <ng-container matColumnDef="remove">
                    <th mat-header-cell *matHeaderCellDef>Remove</th>
                    <td mat-cell *matCellDef="let element">
                        <button matTooltip="Remove Agent"  (click)="deleteAgent(element.id)" mat-icon-button color="warn" matTooltipPosition="after">
                            <mat-icon>delete</mat-icon>
                        </button>
                    </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;"class="example-element-row"     ></tr>

            </table>
        </div>
    `
})

export class AgentsComponent implements AfterViewInit {
    title: string = "Agents";
    displayedColumns: string[] = ['number', 'id', 'name', 'host', 'remove'];
    dataSource: AgentDataSource = new AgentDataSource(this.store.select(getAgents));

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort, { static: true }) sort: MatSort;
    @ViewChild(MatTable, { static: true }) table: MatTable<any>;

    constructor(private store: Store<AgentsState>, private translate: TranslateService) {
        translate.setDefaultLang("en");
        translate.use("en");
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
        this.table.renderRows();
    }

    public deleteAgent(agentId: string) {
        this.store.dispatch(new RemoveCurrentAgentAction(agentId));
        this.reload();
    }
}
