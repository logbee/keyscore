import {map} from "rxjs/operators";
import {EditingPipelineModel} from "@keyscore-manager-models/src/main/pipeline-model/EditingPipelineModel";
import {FilterDescriptor} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";

export const filterDescriptorsNotInPipeline = () => {
    return map(([pipeline, filterDescriptors]: [EditingPipelineModel, FilterDescriptor[]]) => {
        if (!pipeline) return [pipeline, filterDescriptors];

        const descriptorsOfPipeline: FilterDescriptor[] = filterDescriptors.filter(descriptor =>
            pipeline.blueprints.some(blueprint => blueprint.descriptor.uuid === descriptor.descriptorRef.uuid)
        );
        return [pipeline, descriptorsOfPipeline];
    })
};
