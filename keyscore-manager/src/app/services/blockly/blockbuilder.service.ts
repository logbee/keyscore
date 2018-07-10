import {Blockly} from "node-blockly/browser";
import {Injectable} from "@angular/core";
import {InternalPipelineConfiguration} from "../../pipelines/pipelines.model";

declare var Blockly: any;

@Injectable()
export class BlockBuilderService {

    public toBlocklyPipeline(pipelineConfiguration: InternalPipelineConfiguration): string {
        const parser = new DOMParser();
        const serializer = new XMLSerializer();
        const xml =
            `<xml>
                <block type="pipeline_configuration" x="10" y="10" movable="false" deletable="false">
                    <field name="PIPELINE_NAME">${pipelineConfiguration.name}</field>
                    <field name="PIPELINE_DESCRIPTION">${pipelineConfiguration.description}</field>
                    <statement name="FILTER"></statement>
                </block>
             </xml>`;
        const xmlDoc = parser.parseFromString(xml, "text/xml");

        pipelineConfiguration.filters.forEach((filter, index, array) => {
            const currentXmlBlock = xmlDoc.createElement("block");
            currentXmlBlock.setAttribute("type", filter.descriptor.name);
            filter.parameters.forEach((parameter) => {
                const currentXmlField = xmlDoc.createElement("field");
                currentXmlField.setAttribute("name", parameter.name);
                currentXmlField.appendChild(xmlDoc.createTextNode(parameter.value));
                currentXmlBlock.appendChild(currentXmlField);
            });
            if (index < array.length - 1) {
                currentXmlBlock.appendChild(xmlDoc.createElement("next"));
            }
            if (index === 0) {
                xmlDoc.getElementsByName("FILTER").item(0).appendChild(currentXmlBlock);
            } else {
                xmlDoc.getElementsByTagName("next").item(index - 1).appendChild(currentXmlBlock);
            }
        });

        return serializer.serializeToString(xmlDoc);
    }
}
