import {EXTRACT_FROM_SELECTED_BLOCK_SUCCESS, PreviewActions, RESET_PREVIEW_STATE} from "../actions/preview.actions";
import {Dataset} from "../../models/dataset/Dataset";
import {ValueJsonClass} from "../../models/dataset/Value";
import {v4 as uuid} from "uuid";
import {
    ChangeType,
    DatasetTableModel,
    DatasetTableRecordModel,
    DatasetTableRowModel,
    DatasetTableRowModelData
} from "../../models/dataset/DatasetTableModel";
import {Record} from "../../models/dataset/Record";
import {lifecycleHookToNodeFlag} from "@angular/compiler/src/view_compiler/provider_compiler";
import {Field} from "../../models/dataset/Field";

export class PreviewState {
    public outputDatasets: Dataset[];
    public dummyDataset: Dataset;
    public datasetModels: DatasetTableModel[];
    public extractFinish: boolean;
}
export const initalPreviewState: PreviewState = {
    outputDatasets: [],
    datasetModels: [],
    dummyDataset: {
        metaData: {labels: []},
        records: [{fields: [{name: "dummy", value: {jsonClass: ValueJsonClass.TextValue, value: "dummy"}}]}]
    },
    extractFinish: false
};



export function PreviewReducer(state: PreviewState = initalPreviewState, action: PreviewActions): PreviewState {
    const result: PreviewState = Object.assign({}, state);

    switch (action.type) {
        case EXTRACT_FROM_SELECTED_BLOCK_SUCCESS:
            //TODO: Evaluate what this does/or if it works
            if (!action.output.map(elem => elem.metaData === undefined).length) {
                action.output.map(dataset => {
                    dataset.metaData.labels.push(
                        {
                            name: "io.logbee.keyscore.manager.live-editing.id",
                            value: {jsonClass: ValueJsonClass.TextValue, value: uuid()}
                        }
                    )
                });
            }
            //TODO: transform outputdatasets into DataTableModel
            result.outputDatasets = action.output;
            const models: DatasetTableModel[] = [];
            result.outputDatasets.forEach((dataset) => {
                // TODO: rework mehtods from live-editing reducer and use them here
                let model = createDatasetTableModel(dataset, result.dummyDataset);
                models.push(model)
            });
            result.datasetModels = models;
            result.extractFinish = true;
            return result;
        case RESET_PREVIEW_STATE:
            return Object.assign({}, initalPreviewState);
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

