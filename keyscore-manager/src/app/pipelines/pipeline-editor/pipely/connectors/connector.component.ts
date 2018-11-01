import {Component, Input} from "@angular/core";

@Component({
    selector: 'g[svg-connector]',
    template: `
        <svg:path attr.d="{{connectionTypes.get(connectionType).connectorPath}}"
                  attr.fill="{{color}}" />
        <svg:path
                attr.d="{{connectionTypes.get(connectionType).indicatorPath}}" fill="none" stroke-width="25px"
                attr.stroke="{{isDroppable ? droppableIndicatorColor : defaultIndicatorColor}}"/>
        <svg:path attr.d="{{connectionTypes.get(connectionType).selectedPath}}"
                  fill="none" attr.stroke="{{selectedColor}}" attr.stroke-width="{{isSelected ? '30px' : '0px'}}"/>

    `

})

export class ConnectorComponent {
    @Input() isDroppable: boolean;
    @Input() isSelected: boolean = false;
    @Input() connectionType: string;
    @Input() color:string;

    private readonly droppableIndicatorColor = "lime";
    private readonly defaultIndicatorColor = "white";
    private readonly selectedColor="#365880";
    private readonly connectionTypes: Map<string, { connectorPath: string, indicatorPath: string ,selectedPath: string}> = new Map(
        [
            ["default-out", {
                connectorPath: `M876.215,453.784v113.5H677.75V0.5h197.465v134.041l-0.097,0.781l86.097,
           114.861l-0.064,87.628l-86.032,115.517L886.215,455.784z`,
                indicatorPath: `M865.215,0.5 v134.041l-0.097,0.781l86.097,
114.861l-0.064,87.628l-86.032,115.517L865.215,455.784v113.5`,
                selectedPath:`M675.75,15 H865.215 v119.541l-0.097,0.781l86.097,
114.861l-0.064,87.628l-86.032,115.517L865.215,455.784v97 H675.75`
            }],
            ["default-in", {
                connectorPath: `M292.75 567.429H0.536V453.792l0.096,0.457v-0.587l85.557-115.387l0.064-87.628
	L0.632,135.915v-0.91l-0.096,0.781V0.5H292.75 M0.632,453.662l-0.096,0.13l0.096,0.457V453.662z M0.632,135.005l-0.096,0.781
	l0.096,0.129V135.005z`,
                indicatorPath: `M10.536 567.429 V453.792l0.096,0.457v-0.587l85.557-115.387l0.064-87.628
L10.536,135.915v-0.91 l-0.096,0.781V0.5`,
                selectedPath:`M294.75 552.429 H10.536 V453.792l0.096,0.457v-0.587l85.557-115.387l0.064-87.628
L10.536,135.915v-0.91 l-0.096,0.781V15 H294.75`
            }],
            ["no-connection-in", {
                connectorPath: `M0 0.5 H 292.75 V 567.429 H 0 V 0.5`,
                indicatorPath: ``,
                selectedPath:`M0 15 H 294.75 M 294.75 553.429 H 15 V 15`
            }],
            ["no-connection-out", {
                connectorPath: `M677.75 0.5 h 292.75 V 567.429 H 677.75 V 0.5`,
                indicatorPath: ``,
                selectedPath:`M675.75 15 h 279.75 V 552.429 H 675.75`
            }]
        ]
    );

}