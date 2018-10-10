import {MatTableDataSource} from "@angular/material";
import {BehaviorSubject, Observable} from "rxjs/index";
import {PipelineTableModel} from "./PipelineTableModel";

export class PipelineDataSource extends MatTableDataSource<PipelineTableModel>{
    constructor(pipelineObjects$: Observable<PipelineTableModel[]>) {
     super();
        pipelineObjects$.subscribe( pipelines => {
            this.data = pipelines;
        }


        );
        this.filterPredicate = (pipeline: PipelineTableModel, filter: string) => {
            let searchString = filter.trim().toLowerCase();
            return pipeline.uuid.includes(searchString) || pipeline.name.toLowerCase().includes(searchString) || pipeline.health.toLowerCase().includes(searchString);
        };


    }
    connect(): BehaviorSubject<PipelineTableModel[]> {
        return super.connect()
    }
    disconnect() {

    }
}