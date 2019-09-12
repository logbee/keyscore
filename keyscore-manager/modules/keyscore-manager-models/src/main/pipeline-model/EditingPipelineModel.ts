import {Blueprint, Configuration, generateRef, PipelineBlueprint, Ref, TextValue} from "@keyscore-manager-models";

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
                }
                ]
            }
        },
        blueprints: [],
        configurations: []
    }
};