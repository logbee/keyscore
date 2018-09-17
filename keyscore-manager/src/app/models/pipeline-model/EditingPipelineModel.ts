import {Blueprint, PipelineBlueprint} from "../blueprints/Blueprint";
import {Configuration} from "../common/Configuration";

export interface EditingPipelineModel{
    pipelineBlueprint:PipelineBlueprint;
    blueprints:Blueprint[];
    configurations:Configuration[];
}