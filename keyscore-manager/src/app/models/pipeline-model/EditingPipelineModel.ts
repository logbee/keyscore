import {Blueprint, PipelineBlueprint} from "../blueprints/Blueprint";
import {Configuration} from "../common/Configuration";
import {generateRef, Ref} from "../common/Ref";

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
                    name: "displayName",
                    value: {
                        jsonClass: "",
                        value: "New Pipeline"
                    }
                }, {
                    name: "description",
                    value: {
                        jsonClass: "",
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