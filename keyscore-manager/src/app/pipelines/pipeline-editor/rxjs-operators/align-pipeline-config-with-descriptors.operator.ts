import {map} from "rxjs/operators";
import {EditingPipelineModel} from "@keyscore-manager-models/src/main/pipeline-model/EditingPipelineModel";
import {FilterDescriptor} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";
import {PipelineConfigurationChecker} from "@/app/pipelines/services/pipeline-configuration-checker.service";
import {ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";

export const alignPipelineConfigWithDescriptors = (configurationChecker: PipelineConfigurationChecker) => {
    return map(([pipeline, descriptors]: [EditingPipelineModel, FilterDescriptor[]]) =>
        configurationChecker.alignConfigWithDescriptor(pipeline, descriptors)
    )
};
