/*
import {Injectable} from "@angular/core";
import {Blockly} from "node-blockly/browser";
import {v4 as uuid} from "uuid";
import {extractTopLevelJSONObjectsFromString, mapFromSeparatedString} from "../../util";
import {FilterDescriptor} from "../../models/filter-model/FilterDescriptor";
import {FilterConfiguration} from "../../models/filter-model/FilterConfiguration";
import {PipelineConfiguration} from "../../models/pipeline-model/PipelineConfiguration";
import {Parameter} from "../../models/pipeline-model/parameters/Parameter";
import {ParameterDescriptor} from "../../models/pipeline-model/parameters/ParameterDescriptor";

declare var Blockly: any;

@Injectable()
export class ToolBarBuilderService {

    public createToolbar(filterDescriptors: FilterDescriptor[], categories: string[]): string {
        const parser = new DOMParser();
        const serializer = new XMLSerializer();
        const xml = '<xml xmlns="http://www.w3.org/1999/xhtml" id="toolbox" style="display: none;"></xml>';
        const xmlDoc = parser.parseFromString(xml, "text/xml");
        Blockly.HSV_SATURATION = 0.70;
        Blockly.HSV_VALUE = 0.80;

        categories.forEach((cat, index, array) => {
            const currentXmlCategory = xmlDoc.createElement("category");
            currentXmlCategory.setAttribute("name", cat);
            currentXmlCategory.setAttribute("colour", this.calculateColor(index, array.length).toString());
            filterDescriptors.forEach((descriptor) => {
                if (descriptor.category === cat) {
                    const color = this.calculateColor(index, array.length);
                    this.createBlock(descriptor, color, array);
                    const currentXmlBlock = xmlDoc.createElement("block");
                    currentXmlBlock.setAttribute("type", descriptor.name);
                    currentXmlCategory.appendChild(currentXmlBlock);
                }
            });
            xmlDoc.getElementById("toolbox").appendChild(currentXmlCategory);
        });

        const xmlString = serializer.serializeToString(xmlDoc);
        return xmlString;
    }

    public createPipelineBlock() {
        Blockly.Blocks.pipeline_configuration = {
            init() {
                this.setColour(45);
                this.appendDummyInput().appendField("Pipeline name: ")
                    .appendField(new Blockly.FieldTextInput("New Pipeline"), "PIPELINE_NAME");
                this.appendDummyInput().appendField("Pipeline description: ")
                    .appendField(new Blockly.FieldTextInput("description"), "PIPELINE_DESCRIPTION");
                this.appendStatementInput("FILTER").appendField("Filter").setCheck("pipeline_base");

            }
        };

        Blockly.JavaScript.pipeline_configuration = (block: Blockly.Block) => {
            const id: string = "0";
            const name: string = block.getFieldValue("PIPELINE_NAME");
            const description: string = block.getFieldValue("PIPELINE_DESCRIPTION");
            const filterConfigurations: FilterConfiguration[] =
                extractTopLevelJSONObjectsFromString(
                    Blockly.JavaScript.statementToCode(block, "FILTER")
                ) as FilterConfiguration[];

            const pipeline: PipelineConfiguration = {
                id,
                name,
                description,
                source: filterConfigurations[0],
                sink: filterConfigurations[filterConfigurations.length - 1],
                filter: filterConfigurations.slice(1, filterConfigurations.length - 1)
            };

            return JSON.stringify(pipeline);

        };
    }

    private createBlock(descriptor: FilterDescriptor, color: number, categories: string[]) {
        const that = this;
        Blockly.Blocks[descriptor.name] = {
            init() {
                this.setPreviousStatement(
                    descriptor.previousConnection.isPermitted,
                    descriptor.previousConnection.connectionType.length ?
                        descriptor.previousConnection.connectionType :
                        categories);
                this.setNextStatement(
                    descriptor.nextConnection.isPermitted,
                    descriptor.nextConnection.connectionType.length ?
                        descriptor.nextConnection.connectionType :
                        categories);
                this.setColour(color);
                this.appendDummyInput()
                    .appendField(descriptor.displayName);
                this.setTooltip(descriptor.description);
                descriptor.parameters.forEach((p) => {
                    switch (p.jsonClass) {
                        case "TextParameterDescriptor":
                            this.appendDummyInput().appendField(p.displayName)
                                .appendField(new Blockly.FieldTextInput(p.displayName), p.name);
                            break;
                        case "IntParameterDescriptor":
                            this.appendDummyInput().appendField(p.displayName)
                                .appendField(new Blockly.FieldNumber("0"), p.name);
                            break;
                        case "BooleanParameterDescriptor":
                            this.appendDummyInput().appendField(p.displayName)
                                .appendField(new Blockly.FieldCheckbox("FALSE"), p.name);
                            break;
                        default:
                            this.appendDummyInput().appendField(p.displayName)
                                .appendField(new Blockly.FieldTextInput(p.displayName), p.name);
                            break;

                    }
                });
            }
        };

        Blockly.JavaScript[descriptor.name] = (block: Blockly.Block) => {
            const parameters = descriptor.parameters.map((p) => {
                p.value = block.getFieldValue(p.name);
                return p;
            }).map((p) => that.parameterDescriptorToParameter(p));

            const filterConfig: FilterConfiguration = {
                id: uuid(),
                descriptor,
                parameters
            };

            return JSON.stringify(filterConfig);
        };

    }

    private parameterDescriptorToParameter(parameterDescriptor: ParameterDescriptor): Parameter {
        let type = parameterDescriptor.jsonClass;
        switch (type) {
            case "ListParameterDescriptor":
                type = "TextListParameter";
                parameterDescriptor.value = parameterDescriptor.value.split(",");
                break;
            case "MapParameterDescriptor":
                type = "TextMapParameter";
                parameterDescriptor.value = mapFromSeparatedString(
                    parameterDescriptor.value, ",", ":"
                );
                break;
            case "TextParameterDescriptor":
                type = "TextParameter";
                break;
            case "IntParameterDescriptor":
                type = "IntParameter";
                parameterDescriptor.value = +parameterDescriptor.value;
                break;
            case "BooleanParameterDescriptor":
                type = "BooleanParameter";
                parameterDescriptor.value = parameterDescriptor.value !== "FALSE";
                break;
        }
        return {name: parameterDescriptor.name, value: parameterDescriptor.value, jsonClass: type};
    }

    private calculateColor(currentCategory: number, categoryCount: number) {
        return 360 / categoryCount * currentCategory;
    }

}
*/
