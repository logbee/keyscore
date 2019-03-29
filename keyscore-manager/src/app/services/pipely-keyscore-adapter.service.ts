import {Injectable} from "@angular/core";
import {BlockDescriptor} from "../pipelines/pipeline-editor/pipely/models/block-descriptor.model";
import {FilterDescriptorJsonClass, ResolvedFilterDescriptor} from "../../../modules/keyscore-manager-models/src/main/descriptors/FilterDescriptor";

@Injectable()
export class PipelyKeyscoreAdapter {
    resolvedParameterDescriptorToBlockDescriptor(descriptor:ResolvedFilterDescriptor):BlockDescriptor{
        let previousConnection = {
            connectableTypes: descriptor.jsonClass === FilterDescriptorJsonClass.SourceDescriptor ? [] : ['default-out'],
            connectionType: descriptor.jsonClass === FilterDescriptorJsonClass.SourceDescriptor ? 'no-connection-in' : 'default-in'
        };
        let nextConnection = {
            connectableTypes: descriptor.jsonClass === FilterDescriptorJsonClass.SinkDescriptor ? [] : ['default-in'],
            connectionType: descriptor.jsonClass === FilterDescriptorJsonClass.SinkDescriptor ? 'no-connection-out' : 'default-out'
        };

        let blockDescriptor = {
            ref:descriptor.descriptorRef,
            displayName:descriptor.displayName,
            description:descriptor.description,
            previousConnection:previousConnection,
            nextConnection:nextConnection,
            parameters:descriptor.parameters,
            categories:descriptor.categories

        };
        if(descriptor.icon){
            return{
                ...blockDescriptor,
                icon:descriptor.icon
            }
        }else{
            return blockDescriptor;
        }
    }
}