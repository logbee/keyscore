import {EventEmitter} from "@angular/core";
import {Value} from "@keyscore-manager-models";

export interface ValueComponent{
    value:Value;
    disabled:boolean;
    label:string;
    showLabel:boolean;
    keyUpEnter:EventEmitter<Event>;
    changed:EventEmitter<Value>;
}