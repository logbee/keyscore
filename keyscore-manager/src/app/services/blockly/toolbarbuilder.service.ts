import {Injectable} from "@angular/core";
import {FilterDescriptor} from "../../streams/streams.model";
import 'jquery';

@Injectable()
export class ToolBarBuilderService {
    constructor() {
    }

    createToolbar(filterDescriptors:FilterDescriptor[],categories:string[]){
        let xml = '<xml xmlns="http://www.w3.org/1999/xhtml" id="toolbox" style="display: none;"></xml>';
        let xmlDoc = jQuery.parseXML(xml);
        let $xml = jQuery(xmlDoc);
        let $root = $xml.find("xml");

        categories.forEach(cat =>{
            $root.append("category").attr("name",cat);
            filterDescriptors.forEach(descriptor =>{
                if(descriptor.category === cat){
                    $root.find('[name='+cat+']').append(descriptor.name)
                }
            })
        })

        console.log($root);
    }

}