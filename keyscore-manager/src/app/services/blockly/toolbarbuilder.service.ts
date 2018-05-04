import {Injectable} from "@angular/core";
import {FilterDescriptor} from "../../streams/streams.model";
import {Blockly} from "node-blockly/browser";
import {StreamBuilderService} from "../streambuilder.service";

declare var Blockly: any;

@Injectable()
export class ToolBarBuilderService {

    constructor(streamBuilder:StreamBuilderService) {
    }

    createToolbar(filterDescriptors: FilterDescriptor[], categories: string[]): string {
        let parser = new DOMParser();
        let serializer = new XMLSerializer();
        let xml = '<xml xmlns="http://www.w3.org/1999/xhtml" id="toolbox" style="display: none;"></xml>';
        let xmlDoc = parser.parseFromString(xml, "text/xml");

        categories.forEach((cat,index,array) => {
            let currentXmlCategory = xmlDoc.createElement("category");
            currentXmlCategory.setAttribute("name", cat);
            currentXmlCategory.setAttribute("colour",this.calculateColor(index,array.length).toString());
            filterDescriptors.forEach(descriptor => {
                if (descriptor.category === cat) {
                    let color = this.calculateColor(index,array.length);
                    this.createBlock(descriptor,color,array);
                    let currentXmlBlock = xmlDoc.createElement("block");
                    currentXmlBlock.setAttribute("type", descriptor.name);
                    currentXmlCategory.appendChild(currentXmlBlock);
                }
            })
            xmlDoc.getElementById("toolbox").appendChild(currentXmlCategory);
        })

        let xmlString = serializer.serializeToString(xmlDoc);
        console.log("toolbox: " + xmlString);
        return xmlString;
    }

    createStreamBlock() {
        Blockly.Blocks['stream_configuration'] = {
            init: function () {
                this.setColour(45);
                this.appendDummyInput().appendField('Stream name: ')
                    .appendField(new Blockly.FieldTextInput('New Stream'));
                this.appendDummyInput().appendField('Stream description: ')
                    .appendField(new Blockly.FieldTextInput('description'));
                this.appendStatementInput('FILTER').appendField('Filter').setCheck('stream_base');

            }
        }
    }

    private createBlock(descriptor: FilterDescriptor,color:number,categories:string[]) {
        let that = this;
        Blockly.Blocks[descriptor.name] = {
            init: function () {
                this.setPreviousStatement(descriptor.previousConnection.isPermitted, descriptor.previousConnection.connectionType.length ? descriptor.previousConnection.connectionType : categories);
                this.setNextStatement(descriptor.nextConnection.isPermitted,descriptor.nextConnection.connectionType.length ? descriptor.nextConnection.connectionType : categories);
                this.setColour(color);
                this.appendDummyInput()
                    .appendField(descriptor.displayName);
                this.setTooltip(descriptor.description);
                descriptor.parameters.forEach(p => {
                    switch (p.kind) {
                        case 'text':
                            this.appendDummyInput().appendField(p.displayName).appendField(new Blockly.FieldTextInput(p.displayName), p.name);
                            break;
                        case 'int':
                            this.appendDummyInput().appendField(p.displayName).appendField(new Blockly.FieldNumber('0'), p.name);
                            break;
                        case 'boolean':
                            this.appendDummyInput().appendField(p.displayName).appendField(new Blockly.FieldCheckbox('FALSE'), p.name);
                            break;
                        default:
                            this.appendDummyInput().appendField(p.displayName).appendField(new Blockly.FieldTextInput(p.displayName),p.name);
                            break;


                    }
                })
            }
        };

        Blockly.JavaScript[descriptor.name] = function(block:Blockly.Block){
            descriptor.parameters.forEach(p => {
                //p.value = block.getFieldValue(p.name);
            })
        }


    }

    private calculateColor(currentCategory:number, categoryCount:number){
        return 360/categoryCount*currentCategory;
    }


}