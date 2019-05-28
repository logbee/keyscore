import {MetaData} from "../common/MetaData";
import {Value} from "./Value";


export class DatasetTableModel {
    inputMetadata: MetaData;
    outputMetadata: MetaData;
    records: DatasetTableRecordModel[];

    constructor(inputMetadata: MetaData, outputMetadata: MetaData, records: DatasetTableRecordModel[]) {
        this.inputMetadata = inputMetadata;
        this.outputMetadata = outputMetadata;
        this.records = records
    }

}

export class DatasetTableRecordModel {
    // inputRecord
    // outputRecord
    rows: DatasetTableRowModel[];

    constructor(rows: DatasetTableRowModel[]) {
        this.rows = rows;
    }
}

export class DatasetTableRowModel {
    input: DatasetTableRowModelData;
    output: DatasetTableRowModelData;

    constructor(input: DatasetTableRowModelData, output: DatasetTableRowModelData) {
        this.input = input;
        this.output = output;
    }
}

export class DatasetTableRowModelData {
    name: string;
    value: Value;
    type: string;
    change: ChangeType;

    constructor(name: string, type: string, value: Value, change: ChangeType) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.change = change;
    }
}

export enum ChangeType {
    Unchanged = "unchanged",
    Modified = "modified",
    Deleted = "deleted",
    Added = "added",
}



