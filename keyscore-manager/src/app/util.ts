import 'jquery';

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


