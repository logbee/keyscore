import {Injectable} from "@angular/core";
import {
    FilterConfiguration, FilterDescriptor, FilterModel, Parameter, ParameterDescriptor, PipelineConfiguration,
    PipelineModel
} from "../pipelines/pipelines.model";
import {p} from "@angular/core/src/render3";

@Injectable()
export class PipelineBuilderService {
    constructor() {

    }

    //TODO: For now source has to be the first element and sink the last. GUI should prevent false input
    toPipeline(pipeline: PipelineModel, descriptorList: Array<FilterDescriptor>): PipelineConfiguration {



        let sourceConf: FilterConfiguration = this.filterModelToConfiguration(pipeline.filters[0],descriptorList.find(descriptor => descriptor.name == pipeline.filters[0].name ));
        let sinkConf: FilterConfiguration = this.filterModelToConfiguration(pipeline.filters[pipeline.filters.length - 1], descriptorList.find(descriptor => descriptor.name == pipeline.filters[pipeline.filters.length -1].name));
        let filterConf: FilterConfiguration[] = [];
        pipeline.filters.forEach(fm => {
            if (fm.id != sourceConf.id && fm.id != sinkConf.id) {
                filterConf.push(this.filterModelToConfiguration(fm, descriptorList.find(descriptor => descriptor.name == fm.name)));
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

    private filterModelToConfiguration(filter: FilterModel, filterDescriptor: FilterDescriptor): FilterConfiguration {
        let confParameters = filter.parameters.map(p => this.parameterDescriptorToParameter(p));
        return {id: filter.id, descriptor: filterDescriptor, parameters: confParameters};
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