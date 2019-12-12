import {AfterViewInit, Component, EventEmitter, Input, Output, ViewChild} from "@angular/core";
import {PipelineDataSource} from "@/app/data-source/pipeline-data-source";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {Health} from "@keyscore-manager-models/src/main/common/Health";
import {PipelineTableModel} from "@/app/pipelines/PipelineTableModel";


@Component({
    selector: "pipelines-overview",
    template: `
        <table fxFlex="90%" mat-table matSort [dataSource]="dataSource"
               class="table-position mat-elevation-z8">

            <ng-container matColumnDef="health">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
                <td mat-cell *matCellDef="let pipelineTableModel">
                    <resource-health [health]="pipelineTableModel.health"></resource-health>
                </td>
            </ng-container>

            <ng-container matColumnDef="name">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Name</th>
                <td mat-cell *matCellDef="let pipelineTableModel">
                    {{pipelineTableModel.name}}
                </td>
            </ng-container>

            <ng-container matColumnDef="uuid">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
                <td mat-cell *matCellDef="let pipelineTableModel">
                    {{pipelineTableModel.uuid}}
                </td>
            </ng-container>

            <ng-container matColumnDef="deploy">
                <th mat-header-cell *matHeaderCellDef mat-sort-header></th>
                <td mat-cell *matCellDef="let pipelineTableModel">
                    <mat-slide-toggle [checked]="isRunning(pipelineTableModel)"
                                      (change)="onChange($event.checked, pipelineTableModel)">
                    </mat-slide-toggle>
                    <button mat-icon-button (click)="onEditPipeline(pipelineTableModel.uuid)">
                        <mat-icon>settings</mat-icon>
                    </button>
                </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="['health', 'uuid', 'name', 'deploy']"></tr>
            <tr mat-row *matRowDef="let row; columns: ['health', 'uuid', 'name', 'deploy'];"
                class="example-element-row"></tr>
        </table>
    `
})

export class PipelineOverviewComponent implements AfterViewInit {

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;

    @Input() dataSource: PipelineDataSource;
    @Output() editPipeline: EventEmitter<string> = new EventEmitter();
    @Output() deployPipeline: EventEmitter<[string, boolean]> = new EventEmitter();

    private healthType = Health;

    public ngAfterViewInit(): void {
        console.log("Datasource: ", this.dataSource);
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
    }

    onEditPipeline(uuid: string) {
        this.editPipeline.emit(uuid);
    }

    private onChange(checked: boolean, pipeline: PipelineTableModel): void {
        // console.log('pipeline', checked, pipeline);
        this.deployPipeline.emit([pipeline.uuid, checked]);
    }

    private isRunning(pipeline: PipelineTableModel): boolean {
        return pipeline.health != Health.Unknown;
    }
}
