import {AfterViewInit, Component, ViewChild} from "@angular/core";
import {Store} from "@ngrx/store";

import "../style/global-table-styles.css";
import {MatPaginator, MatSort} from "@angular/material";
import {selectBlueprints} from "./resources.reducer";
import {BlueprintDataSource} from "../dataSources/BlueprintDataSource";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {FieldNameHint, ParameterDescriptorJsonClass} from "../models/parameters/ParameterDescriptor";
import {FilterDescriptorJsonClass, ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {Configuration} from "../models/common/Configuration";

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
                    <td mat-cell *matCellDef="let element">{{element?.ref.uuid}}</td>
                </ng-container>

                <!--Type Column-->
                <ng-container matColumnDef="jsonClass">
                    <th mat-header-cell *matHeaderCellDef mat-sort-header>Type</th>
                    <td mat-cell *matCellDef="let element">
                        <resource-type [type]="element?.jsonClass"></resource-type>
                    </td>
                </ng-container>

                <!--Expandend Content Column-->
                <ng-container  matColumnDef="expandedDetail">
                    <mat-cell *matCellDef="let blueprint">
                        <json-visualizer
                                class="jsonViewer"
                                [class.visible]="expandedElement === blueprint"
                                [descriptor]="resolvedFDesc"></json-visualizer>
                    </mat-cell>
                </ng-container>

                <!--Defining header row -->
                <tr mat-header-row *matHeaderRowDef="['uuid', 'jsonClass', 'health'];"></tr>

                <!--Defining row with uuid jsonClass and health columns-->
                <tr mat-row *matRowDef="let blueprint; columns: ['uuid', 'jsonClass', 'health'];"
                    class="example-element-row"
                    [class.expanded]="expandedElement === blueprint"
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

    resolvedFDesc: ResolvedFilterDescriptor = {
        descriptorRef: {
            uuid: "b7ee17ad-582f-494c-9f89-2c9da7b4e467"
        },
        name: "io.logbee.keyscore.pipeline.contrib.filter.RemoveFieldsFilterLogic",
        jsonClass: FilterDescriptorJsonClass.FilterDescriptor,
        displayName: "Felder entfernen",
        description: "Filter zum entfernen von Feldern einschließlich ihrer Werte.",
        categories: [{
            name: "contrib.remove-drop",
            displayName: "Entfernen/Verwerfen"
        }],
        parameters:[
            {
                ref:{
                    uuid:"removeFields.fieldsToRemove"
                },
                info:{
                    displayName:"Feld das entfernt werden soll",
                    description:"Feld wird vom Filter entfernt"
                },
                jsonClass:ParameterDescriptorJsonClass.FieldNameListParameterDescriptor,
                descriptor:{
                    jsonClass:ParameterDescriptorJsonClass.FieldNameParameterDescriptor,
                    ref:{
                        uuid:""
                    },
                    info:{
                        displayName:"",
                        description:""
                    },
                    defaultValue:"",
                    validator:null,
                    hint:FieldNameHint.PresentField,
                    mandatory:false
                },
                min:1,
                max:2147483647
            }
        ]
    };
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

    setExpanded(blueprint: any) {
        this.expandedElement = blueprint
    }


}