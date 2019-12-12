import {Component, OnInit, ViewChild} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {MatPaginator, MatSort} from "@angular/material";
import {ResourcesDataSource} from "../data-source/resources-data-source";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {BehaviorSubject, combineLatest, Observable} from "rxjs";
import {selectTableModels} from "./resources.reducer";
import {ResourceTableModel} from "@/../modules/keyscore-manager-models/src/main/resources/ResourceTableModel";

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
        <div fxLayout="column" fxLayoutGap="15px" class="table-wrapper">
            <!--Search Field-->
            <mat-form-field fxFlex="5%" class="search-position">
                <input matInput (keyup)="applyFilter($event.target.value)"
                       placeholder="{{'GENERAL.SEARCH' | translate}}">
                <button mat-button matSuffix mat-icon-button aria-label="Search">
                    <mat-icon>search</mat-icon>
                </button>
            </mat-form-field>

            <!--Resources Table-->
            <table fxFlex="95" mat-table matSort [dataSource]="dataSource$.getValue()"
                   class="mat-elevation-z8 table-position">
                <!--Resource Id Column-->
                <ng-container matColumnDef="uuid">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Id</th>
                    <td mat-cell *matCellDef="let resourceModel">{{resourceModel.blueprint?.ref.uuid}}</td>
                </ng-container>
                
                <!--Resource display name-->
                <ng-container matColumnDef="name">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Name</th>
                    <td mat-cell *matCellDef="let resourceModel">{{resourceModel.descriptor?.displayName}}</td>lk
                </ng-container>

                <!--Resource categories -->
                <ng-container matColumnDef="categories">
                    <th mat-header-cell *matHeaderCellDef>Categories</th>
                    <td mat-cell *matCellDef="let resourceModel">{{getCategories(resourceModel)}}</td>lk
                </ng-container>
                
                <!--Type Column-->
                <ng-container matColumnDef="jsonClass">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Type</th>
                    <td mat-cell *matCellDef="let resourceModel">
                        <stage-type [stageType]="resourceModel.blueprint?.jsonClass"></stage-type>
                    </td>
                </ng-container>

                <!--Expandend Content Column-->
                <ng-container matColumnDef="expandedDetail">
                    <td mat-cell *matCellDef="let resourceModel" [attr.colspan]="4">
                        <json-visualizer class="jsonViewer" [configuration]="resourceModel.configuration" 
                                         [descriptor]="resourceModel.descriptor" [class.visible]="expandedElement === resourceModel.blueprint">>
                        </json-visualizer>
                    </td>
                </ng-container>

                <!--Defining header row -->
                <tr mat-header-row *matHeaderRowDef="['jsonClass','name', 'uuid', 'categories']"></tr>

                <!--Defining row with uuid jsonClass and health columns-->
                <tr mat-row *matRowDef="let resourceModel; columns: ['jsonClass', 'name', 'uuid', 'categories']"
                    class="example-element-row clickable cursor-pointer"
                    [class.expanded]="expandedElement === resourceModel.blueprint"
                    (click)="setExpanded(resourceModel)">
                </tr>

                <!--Defining collapasable rows -->
                <tr mat-row *matRowDef="let resourceModel; columns: ['expandedDetail'];
                 when: isExpansionDetailRow"
                    class="example-detail-row"
                    [@detailExpand]="resourceModel.blueprint === expandedElement ? 'expanded' : 'collapsed'">
                </tr>
            </table>
        </div>
    `
})

export class ResourcesComponent implements OnInit {

    private title: string = "Resources";
    private dataSource$: BehaviorSubject<ResourcesDataSource> = new BehaviorSubject<ResourcesDataSource>(new ResourcesDataSource([]));
    private resourceModels$: Observable<ResourceTableModel[]> = this.store.pipe(select(selectTableModels));

    expandedElement: any;
    isExpansionDetailRow = (i: number) => i % 2 === 1;

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    constructor(private store: Store<any>) {
    }

    ngOnInit() {
        combineLatest(this.resourceModels$).subscribe(([models]) => {
            this.dataSource$.next(new ResourcesDataSource(models));
            this.dataSource$.getValue().paginator = this.paginator;
            this.dataSource$.getValue().sort = this.sort;
        });
    }
    applyFilter(filterValue: string) {
        this.dataSource$.getValue().filter = filterValue;
        if (this.dataSource$.getValue().paginator) {
            this.dataSource$.getValue().paginator.firstPage()
        }
    }

    setExpanded(resourceModel: ResourceTableModel) {
        console.log("Triggered setExpanded");
        if (this.expandedElement === resourceModel.blueprint) {
            this.expandedElement = "";
        } else {
            this.expandedElement = resourceModel.blueprint;
        }
        console.log(this.expandedElement);
    }

    getCategories(resourceModel: ResourceTableModel) {
        let descriptor = resourceModel.descriptor;
        const result: string [] = [];
        if (descriptor) {
            descriptor.categories.forEach(category => {
                result.push(category.displayName)
            });
            return result;
        } else return "";
    }

}