import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {PipelineTableModel} from "../pipelines/PipelineTableModel";
import {PipelineDataSource} from "./pipeline-data-source";

@Injectable()
export class DataSourceFactory {

    public createPipelineDataSource(pipelineObjects: PipelineTableModel[]): PipelineDataSource {
        return new PipelineDataSource(pipelineObjects);
    }
}