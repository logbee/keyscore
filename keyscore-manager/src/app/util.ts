import 'jquery';
import {
    InternalPipelineConfiguration, Parameter, ParameterDescriptor,
    PipelineConfiguration
} from "./pipelines/pipelines.model";

export function deepcopy(source: any, target?: any): any {
    return jQuery.extend(true, target == null ? {} : target, source);
}

export function extractTopLevelJSONObjectsFromString(str: string): any[] {
    let result: any[] = [];
    while (str.length) {
        let firstObject = extractFirstJSONObjectFromString(str);
        result.push(firstObject.firstObject);
        str = firstObject.tail;
    }
    return result;

}

export function extractFirstJSONObjectFromString(str: string): { firstObject: any, tail: string } {
    let firstOpen = -1, firstClose, candidate;
    firstOpen = str.indexOf('{', firstOpen + 1);
    firstClose = firstOpen;
    do {
        firstClose = str.indexOf('}', firstClose + 1);
        candidate = str.substring(firstOpen, firstClose + 1);
        if ((candidate.match(/{/g) || []).length != (candidate.match(/}/g) || []).length) continue;
        try {
            let result = JSON.parse(candidate);
            let tail = str.substr(firstClose + 1, str.length - firstClose);
            return {firstObject: result, tail: tail};
        }
        catch (e) {
            console.log('extractJSONObject: failed to parse candidate');
            firstClose++;
        }
    } while (firstClose < str.length);

    return null;
}

export function mapFromSeparatedString(mapString: string, elementSeparator: string, keyValueSeparator: string) {
    let elementList = mapString.split(elementSeparator);
    let resultAsList = elementList.map(e => e.split(keyValueSeparator));

    let resultMap: Map<string, string> = new Map<string, string>();
    resultAsList.forEach(e => resultMap[e[0]] = e[1]);

    return resultMap;
}

export function toInternalPipelineConfig(pipe: PipelineConfiguration): InternalPipelineConfiguration {
    let filters = [].concat(pipe.source, pipe.filter, pipe.sink);
    return {id: pipe.id, name: pipe.name, description: pipe.description, filters: filters};
}

export function toPipelineConfiguration(pipe: InternalPipelineConfiguration): PipelineConfiguration {
    let filter = deepcopy(pipe.filters, []);
    let source = filter.find(filter => filter.descriptor.previousConnection.connectionType.includes('pipeline_base'));
    filter.splice(filter.indexOf(source), 1);
    let sink = filter.find(filter => !filter.descriptor.nextConnection.isPermitted);
    filter.splice(filter.indexOf(sink), 1);

    return {id: pipe.id, name: pipe.name, description: pipe.description, source: source, filter: filter, sink: sink};
}

export function parameterDescriptorToParameter(parameterDescriptor: ParameterDescriptor): Parameter {
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
            break;
    }
    return {name: parameterDescriptor.name, value: null, parameterType: type};
}

export function zip(arrays){
    return arrays[0].map(function(_,i){
        return arrays.map(function(array){return array[i]})
    });
}


