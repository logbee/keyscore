import {Injectable} from "@angular/core";
import {
    FilterConfiguration, FilterModel, Parameter, ParameterDescriptor, PipelineConfiguration,
    PipelineModel
} from "../pipelines/pipelines.model";

@Injectable()
export class PipelineBuilderService {
    constructor() {

    }

    //TODO: For now source has to be the first element and sink the last. GUI should prevent false input
    toPipeline(pipeline: PipelineModel): PipelineConfiguration {
        let sourceConf: FilterConfiguration = this.filterModelToConfiguration(pipeline.filters[0]);
        let sinkConf: FilterConfiguration = this.filterModelToConfiguration(pipeline.filters[pipeline.filters.length - 1]);
        let filterConf: FilterConfiguration[] = [];
        pipeline.filters.forEach(fm => {
            if (fm.id != sourceConf.id && fm.id != sinkConf.id) {
                filterConf.push(this.filterModelToConfiguration(fm));
            }
        });

        return {
            id: pipeline.id,
            name: pipeline.name,
            description: pipeline.description,
            source: sourceConf,
            sink: sinkConf,
            filter: filterConf
        };
    }

    private filterModelToConfiguration(filter: FilterModel): FilterConfiguration {
        let confParameters = filter.parameters.map(p => this.parameterDescriptorToParameter(p));
        return {id: filter.id, kind: filter.name, parameters: confParameters};
    }

    private parameterDescriptorToParameter(parameterDescriptor: ParameterDescriptor): Parameter {
        let type = parameterDescriptor.kind;
        switch (type) {
            case 'list':
                type = 'list[string]';
                break;
            case 'map':
                type = 'map[string,string]';
                break;
            case 'text':
                type = 'string';
                break;
            case 'int':
                parameterDescriptor.value = +parameterDescriptor.value;
                break;
        }
        return {name: parameterDescriptor.name, value: parameterDescriptor.value, parameterType: type};
    }
}