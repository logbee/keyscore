import {BehaviorSubject} from "rxjs/index";
import {MatTableDataSource} from "@angular/material";
import {ResourceTableModel} from "../models/resources/ResourceTableModel";


export class ResourcesDataSource extends MatTableDataSource<ResourceTableModel> {

    constructor(resourceModels: ResourceTableModel[]) {
        super();
        console.log(resourceModels);
        this.data = resourceModels;

        this.sortingDataAccessor = (resourceModel: ResourceTableModel, property: string) => {
            switch (property) {
                case "name":
                    return resourceModel.descriptor.displayName;
                case "uuid":
                    return resourceModel.blueprint.ref.uuid;
                default:
                    return resourceModel.blueprint[property];
            }
        };
        this.filterPredicate = (resourceModel: ResourceTableModel, filter: string) => {
            let searchString = filter.trim().toLowerCase();
            return resourceModel.blueprint.ref.uuid.includes(searchString) || resourceModel.blueprint.jsonClass.toString().toLowerCase().includes(searchString);
        };
    }

    connect(): BehaviorSubject<ResourceTableModel[]> {
        return super.connect();
    }
    disconnect() {
    }
}

