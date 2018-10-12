import {MetaData} from "../common/MetaData";
import {Field} from "./Field";
import {Meta} from "@angular/platform-browser";
import {Value} from "./Value";

export class DatasetTableModel {
    inputMetadata: MetaData;
    outputMetadata: MetaData;
    rows: DatasetTableRowModel[];

    constructor(inputMetadata: MetaData, outputMetadata: MetaData, rows: DatasetTableRowModel[]) {
        this.inputMetadata = inputMetadata;
        this.outputMetadata = outputMetadata;
        this.rows = rows
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
    constructor(name: string, type: string, value: Value) {
        this.name = name;
        this.value = value;
        this.type = type;
    }
}



