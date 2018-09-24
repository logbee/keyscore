import "jquery";
import {v4 as uuid} from "uuid";
import {
    ParameterDescriptorJsonClass, ParameterDescriptorPackagePrefix,
    ResolvedParameterDescriptor
} from "./models/parameters/ParameterDescriptor";
import {Parameter, ParameterJsonClass, ParameterPackagePrefix} from "./models/parameters/Parameter";

export function deepcopy(source: any, target?: any): any {
    return jQuery.extend(true, target == null ? {} : target, source);
}

export function parameterDescriptorToParameter(parameterDescriptor: ResolvedParameterDescriptor): Parameter {
    let type = parameterDescriptor.jsonClass.toString();
    type = type.substr(type.lastIndexOf('.') +1 );
    type = type.substr(0,type.length - "Descriptor".length);

    return {ref:parameterDescriptor.ref, value: null, jsonClass: ParameterJsonClass[type]};
}

export function zip(arrays) {
    return arrays[0].map((_, i) => {
        return arrays.map((array) => array[i]);
    });
}

function swap<T>(arr: T[], a: number, b: number) {
    if (a >= 0 && a < arr.length && b >= 0 && b < arr.length) {
        const temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
}
