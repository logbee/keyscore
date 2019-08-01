import {EventEmitter} from "@angular/core";
import {Value} from "../models/value.model";

export interface ValueComponent{
    value:Value;
    disabled:boolean;
    changed:EventEmitter<Value>;
}