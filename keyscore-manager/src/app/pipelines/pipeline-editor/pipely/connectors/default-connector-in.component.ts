import {Component, Input} from "@angular/core";

@Component({
    selector: 'g[default-connector-in]',
    template: `
        <svg:path d="M292.75 567.429H0.536V453.792l0.096,0.457v-0.587l85.557-115.387l0.064-87.628
	L0.632,135.915v-0.91l-0.096,0.781V0.5H292.75 M0.632,453.662l-0.096,0.13l0.096,0.457V453.662z M0.632,135.005l-0.096,0.781
	l0.096,0.129V135.005z" style="fill:#365880;stroke:white;stroke-width:0px"></svg:path>
        <svg:path d="M14.536 567.429 V453.792l0.096,0.457v-0.587l85.557-115.387l0.064-87.628
L14.632,135.915v-0.91 l-0.096,0.781V0.5" fill="none" stroke-width="25px"
              attr.stroke="{{connectorIndicatorColor}}"></svg:path>
    

    `

})

export class DefaultConnectorInComponent{
   @Input() connectorIndicatorColor:string;

}