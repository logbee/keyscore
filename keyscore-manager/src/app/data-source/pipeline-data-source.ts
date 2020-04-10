import { MatTableDataSource } from "@angular/material/table";
import {BehaviorSubject, Observable} from "rxjs/index";
import {PipelineTableModel} from "../pipelines/PipelineTableModel";

export class PipelineDataSource extends MatTableDataSource<PipelineTableModel> {
    constructor(pipelineObjects: PipelineTableModel[]) {
        super();
        this.data = pipelineObjects;
        this.filterPredicate = (pipeline: PipelineTableModel, filter: string) => {
            let searchString = filter.trim().toLowerCase();
            return pipeline.uuid.includes(searchString) ||
                pipeline.name.toLowerCase().includes(searchString) ||
                pipeline.health.toLowerCase().includes(searchString);
        };
    }

    connect(): BehaviorSubject<PipelineTableModel[]> {
        return super.connect()
    }

    disconnect() {

    }
}
