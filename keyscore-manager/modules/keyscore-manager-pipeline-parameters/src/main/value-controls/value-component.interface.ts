import {EventEmitter} from "@angular/core";
import {Value} from "../models/value.model";

export interface ValueComponent{
    value:any;
    disabled:boolean;
    changed:EventEmitter<Value>;
}