import {Injectable} from "@angular/core";
import {BlockDescriptor} from "../pipelines/pipeline-editor/pipely/models/block-descriptor.model";
import {FilterDescriptorJsonClass, ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";

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

        return{
            ref:descriptor.descriptorRef,
            displayName:descriptor.displayName,
            description:descriptor.description,
            previousConnection:previousConnection,
            nextConnection:nextConnection,
            parameters:descriptor.parameters,
            categories:descriptor.categories.map(cat => cat.displayName)

        }
    }
}