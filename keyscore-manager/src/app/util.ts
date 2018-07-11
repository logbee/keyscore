import "jquery";
import {
    InternalPipelineConfiguration, Parameter, ParameterDescriptor,
    PipelineConfiguration
} from "./pipelines/pipelines.model";

export function deepcopy(source: any, target?: any): any {
    return jQuery.extend(true, target == null ? {} : target, source);
}

export function extractTopLevelJSONObjectsFromString(str: string): any[] {
    const result: any[] = [];
    while (str.length) {
        const firstObject = extractFirstJSONObjectFromString(str);
        result.push(firstObject.firstObject);
        str = firstObject.tail;
    }
    return result;

}

export function extractFirstJSONObjectFromString(str: string): { firstObject: any, tail: string } {
    let firstOpen = -1;
    let firstClose;
    let candidate;

    firstOpen = str.indexOf("{", firstOpen + 1);
    firstClose = firstOpen;
    do {
        firstClose = str.indexOf("}", firstClose + 1);
        candidate = str.substring(firstOpen, firstClose + 1);
        if ((candidate.match(/{/g) || []).length !== (candidate.match(/}/g) || []).length) {
            continue;
        }
        try {
            const result = JSON.parse(candidate);
            const tail = str.substr(firstClose + 1, str.length - firstClose);
            return {firstObject: result, tail};
        } catch (e) {
            firstClose++;
        }
    } while (firstClose < str.length);

    return null;
}

export function mapFromSeparatedString(mapString: string, elementSeparator: string, keyValueSeparator: string) {
    const elementList = mapString.split(elementSeparator);
    const resultAsList = elementList.map((e) => e.split(keyValueSeparator));

    const resultMap: Map<string, string> = new Map<string, string>();
    resultAsList.forEach((e) => resultMap[e[0]] = e[1]);

    return resultMap;
}

export function separatedStringFromMap(map: any, elementSeparator: string, keyValueSeparator: string) {
    let resultString = "";

    for (const [key, value] of Object.entries(map)) {
        resultString += key + keyValueSeparator + value + elementSeparator;
    }

    return resultString.substr(0, resultString.length - 1);
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

export function parameterDescriptorToParameter(parameterDescriptor: ParameterDescriptor): Parameter {
    let type = parameterDescriptor.kind;
    switch (type) {
        case "list":
            type = "list[string]";
            break;
        case "map":
            type = "map[string,string]";
            break;
        case "text":
            type = "string";
            break;
        case "int":
            break;
    }
    return {name: parameterDescriptor.name, value: null, jsonClass: type};
}

export function zip(arrays) {
    return arrays[0].map((_, i) => {
        return arrays.map((array) => array[i]);
    });
}
