import {Injectable} from "@angular/core";
import {Draggable} from "../models/contract";
import {EditingPipelineModel} from "../../../../models/pipeline-model/EditingPipelineModel";
import {Configuration} from "../../../../models/common/Configuration";
import {BlockConfiguration} from "../models/block-configuration.model";
import {deepcopy} from "../../../../util";
import {DraggableModel} from "../models/draggable.model";
import {
    Blueprint,
    BlueprintJsonClass,
    FilterBlueprint,
    SinkBlueprint,
    SourceBlueprint
} from "../../../../models/blueprints/Blueprint";
import {Ref} from "../../../../models/common/Ref";

@Injectable()
export class PipelineConfiguratorService {

    //TODO: Handle errors, invalid configurations and multiple pipelines

    updatePipelineModel(draggableHeads: Draggable[], pipeline: EditingPipelineModel): EditingPipelineModel {
        let pipelineResult: EditingPipelineModel = deepcopy(pipeline);
        draggableHeads.forEach(draggable => {
            if (draggable.getDraggableModel().blockDescriptor.previousConnection.connectableTypes.length === 0 &&
                draggable.getTail().getDraggableModel().blockDescriptor.nextConnection.connectableTypes.length === 0
            ) {
                this.updateConfigurations(draggable, pipelineResult);
                pipelineResult.blueprints = this.updateBlueprints(draggable, pipelineResult);
                pipelineResult.pipelineBlueprint.blueprints =
                    pipelineResult.blueprints.map(blueprint => blueprint.ref);

            }
        });
        console.log(pipelineResult);
        return pipelineResult;
    }

    private updateBlueprints(draggable: Draggable, pipeline: EditingPipelineModel): Blueprint[] {
        let updatedBlueprints: Blueprint[] = [];
        let next = draggable;
        do {
            const blueprintRef: Ref = next.getDraggableModel().blueprintRef;

            const jsonClass = this.getBlueprintJsonClass(next.getDraggableModel());
            const initialize = {
                jsonClass: jsonClass,
                ref: blueprintRef,
                descriptor: next.getDraggableModel().blockDescriptor.ref,
                configuration: next.getDraggableModel().blockConfiguration.ref
            };
            switch (jsonClass) {
                case BlueprintJsonClass.FilterBlueprint:
                    updatedBlueprints.push({
                        ...initialize,
                        in: next.getDraggableModel().previous.getDraggableModel().blueprintRef,
                        out: next.getDraggableModel().next.blueprintRef
                    } as FilterBlueprint);
                    break;
                case BlueprintJsonClass.SourceBlueprint:
                    updatedBlueprints.push({
                        ...initialize,
                        out: next.getDraggableModel().next.blueprintRef
                    } as SourceBlueprint);
                    break;
                case BlueprintJsonClass.SinkBlueprint:
                    updatedBlueprints.push({
                        ...initialize,
                        in: next.getDraggableModel().previous.getDraggableModel().blueprintRef
                    } as SinkBlueprint);
                    break;
                default:
                    break;
            }
        } while (next = next.getNext());

        return updatedBlueprints;
    }

    private getBlueprintJsonClass(model: DraggableModel) {
        if (model.blockDescriptor.previousConnection.connectableTypes.length === 0 &&
            model.blockDescriptor.nextConnection.connectableTypes.length >= 1
        ) {
            return BlueprintJsonClass.SourceBlueprint;
        }
        if (model.blockDescriptor.nextConnection.connectableTypes.length === 0 &&
            model.blockDescriptor.previousConnection.connectableTypes.length >= 1
        ) {
            return BlueprintJsonClass.SinkBlueprint;
        }

        return BlueprintJsonClass.FilterBlueprint;
    }

    private updateConfigurations(draggable: Draggable, pipeline: EditingPipelineModel) {
        let next = draggable;
        do {
            let configuration: Configuration = this.blockConfigToConfig(next.getDraggableModel().blockConfiguration);
            let filteredConfiguration = pipeline.configurations ?
                pipeline.configurations.findIndex(config =>
                    config.ref.uuid === configuration.ref.uuid) : -1;
            if (filteredConfiguration !== -1) {
                pipeline.configurations[filteredConfiguration] = configuration;
            } else {
                pipeline.configurations.push(configuration);
            }
        } while (next = next.getNext());
    }

    private blockConfigToConfig(blockConfig: BlockConfiguration) {
        return {
            ref: blockConfig.ref,
            parent: null,
            parameters: blockConfig.parameters
        };
    }
}