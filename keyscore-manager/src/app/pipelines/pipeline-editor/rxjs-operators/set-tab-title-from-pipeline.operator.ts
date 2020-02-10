import {EditingPipelineModel} from "@keyscore-manager-models/src/main/pipeline-model/EditingPipelineModel";
import {tap} from "rxjs/operators";
import {Title} from "@angular/platform-browser";
import {TextValue} from "@keyscore-manager-models/src/main/dataset/Value";

export const setTabTitleFromPipeline = (titleService: Title) => {
    return tap((model: EditingPipelineModel) => {
            if (model) {
                const pipelineName = model.pipelineBlueprint.metadata.labels.find(label => label.name === 'pipeline.name');
                if (pipelineName) {
                    titleService.setTitle((pipelineName.value as TextValue).value + " - KEYSCORE");
                }
            }
        }
    )
}
