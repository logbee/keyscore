import {Directive, ElementRef} from "@angular/core";

@Directive({
    selector:"[parameter-list-item]"
})
export class ParameterListItemDirective{

    constructor(public parameterComponentRef:ElementRef){

    }
}