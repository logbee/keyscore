import {
    DATA_PREVIEW_TOGGLE_VIEW,
    EXTRACT_FROM_SELECTED_BLOCK,
    EXTRACT_FROM_SELECTED_BLOCK_FAILURE,
    EXTRACT_FROM_SELECTED_BLOCK_SUCCESS,
    PreviewActions
} from "../actions/preview.actions";


import * as _ from "lodash";
import {Dataset} from "@/../modules/keyscore-manager-models/src/main/dataset/Dataset";
import {ValueJsonClass} from "@/../modules/keyscore-manager-models/src/main/dataset/Value";

export class PreviewState {
    public dummyDataset: Dataset;
    public outputDatasetsMap: Map<string, Dataset[]>;
    public inputDatasetsMap: Map<string, Dataset[]>;
    public selectedBlock: string;
    public isLoadingDatasetsAfter: boolean;
    public isLoadingDatasetsBefore: boolean;
    public loadingErrorAfter: boolean;
    public loadingErrorBefore: boolean;
    public previewVisible: boolean;
}

export const initalPreviewState: PreviewState = {
    outputDatasetsMap: new Map<string, Dataset[]>(),
    inputDatasetsMap: new Map<string, Dataset[]>(),
    dummyDataset: {
        metaData: {labels: []},
        records: [{fields: [{name: "dummy", value: {jsonClass: ValueJsonClass.TextValue, value: "dummy"}}]}]
    },
    selectedBlock: "default",
    isLoadingDatasetsAfter: false,
    isLoadingDatasetsBefore: false,
    loadingErrorAfter: false,
    loadingErrorBefore: false,
    previewVisible: false
};

export function PreviewReducer(state: PreviewState = initalPreviewState, action: PreviewActions): PreviewState {
    let result = _.cloneDeep(state);
    switch (action.type) {
        case EXTRACT_FROM_SELECTED_BLOCK: {
            if (action.where === 'after') {
                result.isLoadingDatasetsAfter = true;
            } else if (action.where === 'before') {
                result.isLoadingDatasetsBefore = true;
            }
            return {...result};
        }
        case EXTRACT_FROM_SELECTED_BLOCK_SUCCESS: {
            if (action.where == "after") {
                result.outputDatasetsMap.set(action.blockId, action.extractedDatsets);
                result.isLoadingDatasetsAfter = false;
                result.loadingErrorAfter = false;

            } else if (action.where == "before") {
                result.inputDatasetsMap.set(action.blockId, action.extractedDatsets);
                result.isLoadingDatasetsBefore = false;
                result.loadingErrorBefore = false;
            }

            return {...result};
        }
        case EXTRACT_FROM_SELECTED_BLOCK_FAILURE: {
            if (action.where === 'after') {
                result.loadingErrorAfter = true;
            } else if(action.where === 'before'){
                result.loadingErrorBefore = true;
            }
            return {...result, inputDatasetsMap: new Map(), outputDatasetsMap: new Map()};
        }
        case DATA_PREVIEW_TOGGLE_VIEW: {
            return {
                ...state,
                previewVisible: action.isDataPreviewVisible,
                loadingErrorAfter: false,
                loadingErrorBefore: false
            };
        }

        default:
            return result;
    }
}
