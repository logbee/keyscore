import 'jquery';

export function deepcopy(source:any, target?:any): any {
    return jQuery.extend(true, target == null ? {} : target, source);
}


