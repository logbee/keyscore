import {EXTRACT_FROM_SELECTED_BLOCK_SUCCESS, PreviewActions, RESET_PREVIEW_STATE} from "../actions/preview.actions";
import {Dataset} from "../../../../modules/keyscore-manager-models/src/main/dataset/Dataset";
import {ValueJsonClass} from "../../../../modules/keyscore-manager-models/src/main/dataset/Value";
import {
    ChangeType,
    DatasetTableModel,
    DatasetTableRecordModel,
    DatasetTableRowModel,
    DatasetTableRowModelData
} from "../../../../modules/keyscore-manager-models/src/main/dataset/DatasetTableModel";
import {Record} from "../../../../modules/keyscore-manager-models/src/main/dataset/Record";
import {Field} from "../../../../modules/keyscore-manager-models/src/main/dataset/Field";
import * as _ from "lodash";
export class PreviewState {
    public dummyDataset: Dataset;
    public outputDatasetModelMap: Map<string, DatasetTableModel[]>;
    public inputDatasetModelMap: Map<string, DatasetTableModel[]>;
    public selectedBlock: string;
}
export const initalPreviewState: PreviewState = {
    outputDatasetModelMap: new Map <string, DatasetTableModel[]>(),
    inputDatasetModelMap: new Map <string, DatasetTableModel[]>(),
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
            if(action.where == "after") {
                if (action.extractedDatsets.length !== 0) {
                    const models: DatasetTableModel[] = [];
                    action.extractedDatsets.forEach((dataset) => {
                        let model = createDatasetTableModel(dataset, result.dummyDataset);
                        models.push(model)
                    });
                    result.outputDatasetModelMap.set(action.blockId, models);
                }
            } else if (action.where == "before") {
                if (action.extractedDatsets.length !== 0) {
                    const models: DatasetTableModel[] = [];
                    action.extractedDatsets.forEach((dataset) => {
                        let model = createDatasetTableModel(dataset, result.dummyDataset);
                        models.push(model)
                    });
                    result.inputDatasetModelMap.set(action.blockId, models);
                }
            }
            return result;
        default:
            return result;
    }
}

function createDatasetTableModel(inputDataset: Dataset, outputDataset: Dataset): DatasetTableModel {
    const recordModels = inputDataset.records.map(record => {
        const fieldnames = record.fields.map(field => field.name);
        const datsetTableRowModels = fieldnames.map(name => {
            return createDatasetTableRowModel(findFieldByName(name, record));
        });
        return new DatasetTableRecordModel(datsetTableRowModels)
    });

    return new DatasetTableModel(inputDataset.metaData, outputDataset.metaData, recordModels)
}

function createDatasetTableRowModel(field: Field): DatasetTableRowModel {
    let datamodel: DatasetTableRowModelData;

    datamodel = new DatasetTableRowModelData(field.name, field.value.jsonClass, field.value, ChangeType.Unchanged);

    return new DatasetTableRowModel(datamodel, datamodel);
}

//TODO: Breaks when there are two fields with the same name !
function findFieldByName(name: string, record: Record): Field {
    return record.fields.find(field => field.name === name);
}

