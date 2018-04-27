import {Injectable} from "@angular/core";
import {
    FilterConfiguration, FilterModel, Parameter, ParameterDescriptor, StreamConfiguration,
    StreamModel
} from "../streams/streams.model";

@Injectable()
export class StreamBuilderService {
    constructor() {

    }

    //TODO: For now source has to be the first element and sink the last. GUI should prevent false input
    toStream(stream: StreamModel): StreamConfiguration {
        let sourceConf: FilterConfiguration = this.filterModelToConfiguration(stream.filters[0]);
        let sinkConf: FilterConfiguration = this.filterModelToConfiguration(stream.filters[stream.filters.length - 1]);
        let filterConf: FilterConfiguration[] = [];
        stream.filters.forEach(fm => {
            if (fm.id != sourceConf.id && fm.id != sinkConf.id) {
                filterConf.push(this.filterModelToConfiguration(fm));
            }
        });

        return {
            id: stream.id,
            name: stream.name,
            description: stream.description,
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