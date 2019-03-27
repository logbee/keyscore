import {Blueprint, PipelineBlueprint} from "../blueprints/Blueprint";
import {Configuration} from "../common/Configuration";
import {generateRef, Ref} from "../common/Ref";
import {ValueJsonClass} from "../dataset/Value";

export interface EditingPipelineModel{
    pipelineBlueprint:PipelineBlueprint;
    blueprints:Blueprint[];
    configurations:Configuration[];
}

export const generateEmptyEditingPipelineModel = (ref:Ref = generateRef()):EditingPipelineModel => {
    return{
        pipelineBlueprint: {
            ref: ref,
            blueprints: [],
            metadata: {
                labels: [{
                    name: "pipeline.name",
                    value: {
                        jsonClass: ValueJsonClass.TextValue,
                        value: "New Pipeline"
                    }
                }, {
                    name: "pipeline.description",
                    value: {
                        jsonClass: ValueJsonClass.TextValue,
                        value: "Your new Pipeline"
                    }
                }
                ]
            }
        },
        blueprints: [],
        configurations: []
    }
};