import {Directive, ViewContainerRef} from "@angular/core";

@Directive({
    selector: '[value-host]'
})
export class ValueDirective {
    constructor(public viewContainerRef: ViewContainerRef) {
    }
}