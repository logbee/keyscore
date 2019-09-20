import {EXTRACT_FROM_SELECTED_BLOCK_SUCCESS, PreviewActions} from "../actions/preview.actions";


import * as _ from "lodash";
import {Dataset} from "@/../modules/keyscore-manager-models/src/main/dataset/Dataset";
import {Record} from "@/../modules/keyscore-manager-models/src/main/dataset/Record";
import {DatasetTableModel, DatasetTableRecordModel, DatasetTableRowModel, DatasetTableRowModelData, ChangeType} from "@/../modules/keyscore-manager-models/src/main/dataset/DatasetTableModel";
import {ValueJsonClass} from "@/../modules/keyscore-manager-models/src/main/dataset/Value";
import {Field} from "@/../modules/keyscore-manager-models/src/main/dataset/Field";

export class PreviewState {
    public dummyDataset: Dataset;
    public outputDatasetsMap: Map<string, Dataset[]>;
    public inputDatasetsMap: Map<string, Dataset[]>;
    public selectedBlock: string;
}

export const initalPreviewState: PreviewState = {
    outputDatasetsMap: new Map<string, Dataset[]>(),
    inputDatasetsMap: new Map<string, Dataset[]>(),
    dummyDataset: {
        metaData: {labels: []},
        records: [{fields: [{name: "dummy", value: {jsonClass: ValueJsonClass.TextValue, value: "dummy"}}]}]
    },
    selectedBlock: "default"
};

export function PreviewReducer(state: PreviewState = initalPreviewState, action: PreviewActions): PreviewState {
    let result = _.cloneDeep(state);
    switch (action.type) {
        case EXTRACT_FROM_SELECTED_BLOCK_SUCCESS:
            if (action.where == "after") {
                if (action.extractedDatsets.length !== 0) {
                    result.outputDatasetsMap.set(action.blockId, action.extractedDatsets);
                }
            } else if (action.where == "before") {
                if (action.extractedDatsets.length !== 0) {
                    result.inputDatasetsMap.set(action.blockId, action.extractedDatsets);
                }
            }
            return result;
        default:
            return result;
    }
}
