import {EXTRACT_FROM_SELECTED_BLOCK_SUCCESS, PreviewActions, RESET_PREVIEW_STATE} from "../actions/preview.actions";
import {Dataset} from "../../models/dataset/Dataset";
import {ValueJsonClass} from "../../models/dataset/Value";
import {
    ChangeType,
    DatasetTableModel,
    DatasetTableRecordModel,
    DatasetTableRowModel,
    DatasetTableRowModelData
} from "../../models/dataset/DatasetTableModel";
import {Record} from "../../models/dataset/Record";
import {Field} from "../../models/dataset/Field";
import * as _ from "lodash";
export class PreviewState {
    public outputDatasets: Dataset[];
    public inputDatasets: Dataset[];
    public dummyDataset: Dataset;
    public outputDatasetModelMap: Map<string, DatasetTableModel[]>;
    public inputDatasetModelMap: Map<string, DatasetTableModel[]>;
    public selectedBlock: string;
}
export const initalPreviewState: PreviewState = {
    outputDatasets: [],
    inputDatasets: [],
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
                result.outputDatasets = action.extractedDatsets;
                if (result.outputDatasets.length !== 0) {
                    const models: DatasetTableModel[] = [];
                    result.outputDatasets.forEach((dataset) => {
                        let model = createDatasetTableModel(dataset, result.dummyDataset);
                        models.push(model)
                    });
                    result.outputDatasetModelMap.set(action.blockId, models);
                }
            } else if (action.where == "before") {
                result.inputDatasets = action.extractedDatsets;
                if (result.inputDatasets.length !== 0) {
                    const models: DatasetTableModel[] = [];
                    result.inputDatasets.forEach((dataset) => {
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

