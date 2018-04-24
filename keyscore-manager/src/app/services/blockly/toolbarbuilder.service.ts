import {Injectable} from "@angular/core";
import {FilterDescriptor} from "../../streams/streams.model";

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
                    /*TODO: generate blocks from descriptor*/
                }
            })
            xmlDoc.getElementById("toolbox").appendChild(currentXmlCategory);
        })

        let xmlString = serializer.serializeToString(xmlDoc);
        console.log("toolbox: "+xmlString);
        return xmlString;
    }

}