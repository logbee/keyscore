import {BehaviorSubject} from "rxjs/index";
import {MatTableDataSource} from "@angular/material";
import {ResourceTableModel} from "../../../modules/keyscore-manager-models/src/main/resources/ResourceTableModel";


export class ResourcesDataSource extends MatTableDataSource<ResourceTableModel> {

    constructor(resourceModels: ResourceTableModel[]) {
        super();
        const rows: ResourceTableModel[] = [];
        resourceModels.forEach(model => {
            rows.push(model, model)
        });
        this.data = rows;

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
            return resourceModel.blueprint.ref.uuid.includes(searchString) ||
                resourceModel.blueprint.jsonClass.toString().toLowerCase().includes(searchString) || resourceModel.descriptor.displayName.toLowerCase().includes(searchString) || this.checkCategories(resourceModel, searchString);
        };
    }

    connect(): BehaviorSubject<ResourceTableModel[]> {
        return super.connect();
    }

    disconnect() {
    }

    checkCategories(resourceModel: ResourceTableModel, searchString: string) {
        let descriptor = resourceModel.descriptor;
        const result: string [] = [];
        if (descriptor) {
            descriptor.categories.forEach(category => {
                result.push(category.displayName.toLowerCase())
            });
            return result.includes(searchString);

        }
    }
}

