import {Blueprint, PipelineBlueprint} from "@keyscore-manager-models/src/main/blueprints/Blueprint";
import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";
import {generateRef, Ref} from "@keyscore-manager-models/src/main/common/Ref";
import {TextValue} from "@keyscore-manager-models/src/main/dataset/Value";

export interface EditingPipelineModel {
    pipelineBlueprint: PipelineBlueprint;
    blueprints: Blueprint[];
    configurations: Configuration[];
}

export const generateEmptyEditingPipelineModel = (ref: Ref = generateRef()): EditingPipelineModel => {
    return {
        pipelineBlueprint: {
            ref: ref,
            blueprints: [],
            metadata: {
                labels: [{
                    name: "pipeline.name",
                    value: new TextValue('New Pipeline')
                }, {
                    name: "pipeline.description",
                    value: new TextValue('Your new Pipeline')
                }, {
                    name: "pipeline.selectedAgent",
                    value: new TextValue("")
                }
                ]
            }
        },
        blueprints: [],
        configurations: []
    }
};