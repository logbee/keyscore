import {Component, Input} from "@angular/core";

@Component({
    selector: 'g[svg-connector]',
    template: `
        <svg:path attr.d="{{connectionTypes.get(connectionType).connectorPath}}"
                  attr.fill="{{color}}" style="stroke:white;stroke-width:0px"/>
        <svg:path
                attr.d="{{connectionTypes.get(connectionType).indicatorPath}}" fill="none" stroke-width="25px"
                attr.stroke="{{isDroppable ? droppableIndicatorColor : defaultIndicatorColor}}"/>
    `

})

export class ConnectorComponent {
    @Input() isDroppable: boolean;
    @Input() connectionType: string;
    @Input() color:string;

    private readonly droppableIndicatorColor = "lime";
    private readonly defaultIndicatorColor = "white";
    private readonly connectionTypes: Map<string, { connectorPath: string, indicatorPath: string }> = new Map(
        [
            ["default-out", {
                connectorPath: `M876.215,453.784v113.5H677.75V0.5h197.465v134.041l-0.097,0.781l86.097,
           114.861l-0.064,87.628l-86.032,115.517L886.215,455.784z`,
                indicatorPath: `M860.215,0.5 v134.041l-0.097,0.781l86.097,
114.861l-0.064,87.628l-86.032,115.517L860.215,455.784v113.5`
            }],
            ["default-in", {
                connectorPath: `M292.75 567.429H0.536V453.792l0.096,0.457v-0.587l85.557-115.387l0.064-87.628
	L0.632,135.915v-0.91l-0.096,0.781V0.5H292.75 M0.632,453.662l-0.096,0.13l0.096,0.457V453.662z M0.632,135.005l-0.096,0.781
	l0.096,0.129V135.005z`,
                indicatorPath: `M14.536 567.429 V453.792l0.096,0.457v-0.587l85.557-115.387l0.064-87.628
L14.632,135.915v-0.91 l-0.096,0.781V0.5`
            }],
            ["no-connection-in", {
                connectorPath: `M0 0.5 H 292.75 V 567.429 H 0 V 0.5`,
                indicatorPath: ``
            }],
            ["no-connection-out", {
                connectorPath: `M677.75 0.5 h 292.75 V 567.429 H 677.75 V 0.5`,
                indicatorPath: ``
            }]
        ]
    );

}