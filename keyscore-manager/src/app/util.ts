import "jquery";
import {InternalPipelineConfiguration} from "./models/pipeline-model/InternalPipelineConfiguration";
import {PipelineConfiguration} from "./models/pipeline-model/PipelineConfiguration";
import {
    ParameterDescriptor, ParameterDescriptorJsonClass,
    ResolvedParameterDescriptor
} from "./models/pipeline-model/parameters/ParameterDescriptor";
import {Parameter} from "./models/pipeline-model/parameters/Parameter";

export function deepcopy(source: any, target?: any): any {
    return jQuery.extend(true, target == null ? {} : target, source);
}

export function toInternalPipelineConfig(pipe: PipelineConfiguration): InternalPipelineConfiguration {
    const filters = [].concat(pipe.source, pipe.filter, pipe.sink);
    return {id: pipe.id, name: pipe.name, description: pipe.description, filters, isRunning: false};
}

export function toPipelineConfiguration(pipe: InternalPipelineConfiguration): PipelineConfiguration {
    const filter = deepcopy(pipe.filters, []);
    const source = filter.find((f) =>
        f.descriptor.previousConnection.connectionType.includes("pipeline_base"));
    filter.splice(filter.indexOf(source), 1);
    const sink = filter.find((f) => !f.descriptor.nextConnection.isPermitted);
    filter.splice(filter.indexOf(sink), 1);

    return {id: pipe.id, name: pipe.name, description: pipe.description, source, filter, sink};
}

export function parameterDescriptorToParameter(parameterDescriptor: ResolvedParameterDescriptor): Parameter {
    let type = parameterDescriptor.jsonClass;
    switch (type) {
        case ParameterDescriptorJsonClass.TextListParameterDescriptor:
            type = "TextListParameter";
            break;
        case ParameterDescriptorJsonClass.FieldListParameterDescriptor:
            type = "TextMapParameter";
            break;
        case ParameterDescriptorJsonClass.TextParameterDescriptor:
            type = "TextParameter";
            break;
        case ParameterDescriptorJsonClass.NumberParameterDescriptor:
            type = "IntParameter";
            break;
        case ParameterDescriptorJsonClass.BooleanParameterDescriptor:
            type = "BooleanParameter";
            break;
    }
    return {name: parameterDescriptor.ref.uuid, value: null, jsonClass: type};
}

export function zip(arrays) {
    return arrays[0].map((_, i) => {
        return arrays.map((array) => array[i]);
    });
}
