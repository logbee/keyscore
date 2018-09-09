import {Directive, ElementRef, Input} from "@angular/core";

@Directive({
    selector:'[connectorType]'
})
export class ConnectorTypeDirective{
    @Input() connectorType:string;
    constructor(private el:ElementRef){
        this.el.nativeElement.setAttribute("default-connection-in","");
    }
}