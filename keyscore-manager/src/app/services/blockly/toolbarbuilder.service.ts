import {Injectable} from "@angular/core";
import {FilterDescriptor} from "../../streams/streams.model";
import {Blockly} from "node-blockly/browser";

declare var Blockly:any;

@Injectable()
export class ToolBarBuilderService {
    constructor() {
    }

    createToolbar(filterDescriptors:FilterDescriptor[],categories:string[]):string{
        let parser = new DOMParser();
        let serializer = new XMLSerializer();
        let xml = '<xml xmlns="http://www.w3.org/1999/xhtml" id="toolbox" style="display: none;"></xml>';
        let xmlDoc = parser.parseFromString(xml,"text/xml");

        categories.forEach(cat =>{
            let currentXmlCategory = xmlDoc.createElement("category");
            currentXmlCategory.setAttribute("name",cat);
            filterDescriptors.forEach(descriptor =>{
                if(descriptor.category === cat){
                    this.createBlock(descriptor);
                    let currentXmlBlock = xmlDoc.createElement("block");
                    currentXmlBlock.setAttribute("type",descriptor.name);
                    currentXmlCategory.appendChild(currentXmlBlock);
                }
            })
            xmlDoc.getElementById("toolbox").appendChild(currentXmlCategory);
        })

        let xmlString = serializer.serializeToString(xmlDoc);
        console.log("toolbox: "+xmlString);
        return xmlString;
    }

    private createBlock(descriptor:FilterDescriptor){
        Blockly.Blocks[descriptor.name] = {
            init: function(){
                this.setPreviousStatement(descriptor.previousConnection.isPermitted);
                this.setNextStatement(descriptor.nextConnection.isPermitted);
                this.appendDummyInput()
                    .appendField(descriptor.displayName);
                descriptor.parameters.forEach(p => {
                    switch (p.kind){
                        case 'text': this.appendDummyInput().appendField(p.displayName).appendField(new Blockly.FieldTextInput(''),p.name);
                        break;
                        case 'int':this.appendDummyInput().appendField(p.displayName).appendField(new Blockly.FieldNumber('0'),p.name);
                        break;
                        case 'boolean': this.append
                    }
                })
            }
        }
    }

}