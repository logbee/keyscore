import {EventEmitter} from "@angular/core";
import {Value} from "@/../modules/keyscore-manager-models/src/main/dataset/Value";

export interface ValueComponent{
    value:Value;
    disabled:boolean;
    label:string;
    showLabel:boolean;
    keyUpEnter:EventEmitter<Event>;
    changed:EventEmitter<Value>;
}