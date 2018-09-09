import {Component, Input} from "@angular/core";

@Component({
    selector: 'g[default-connector-out]',
    template: `
        <svg:path d="M876.215,453.784v113.5H677.75V0.5h197.465v134.041l-0.097,0.781l86.097,
114.861l-0.064,87.628l-86.032,115.517L886.215,455.784z" style="fill:#365880;stroke:white;stroke-width:0px"/>
        <svg:path
                d="M860.215,0.5 v134.041l-0.097,0.781l86.097,
114.861l-0.064,87.628l-86.032,115.517L860.215,455.784v113.5" fill="none" stroke-width="25px"
                attr.stroke="{{connectorIndicatorColor}}"></svg:path>
    

    `

})

export class DefaultConnectorOutComponent{
   @Input() connectorIndicatorColor:string;

}