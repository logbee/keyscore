import {Blueprint} from "../models/blueprints/Blueprint";
import {BehaviorSubject, Observable} from "rxjs/index";
import {MatTableDataSource} from "@angular/material";


export class BlueprintDataSource extends MatTableDataSource<Blueprint> {

    constructor(blueprints$: Observable<Blueprint[]>) {
        super();
        blueprints$.subscribe(blueprints => {

            const rows = [];
            blueprints.forEach(blueprint => rows.push(blueprint, blueprint));

            this.data = rows;
        });
        this.sortingDataAccessor = (blueprint: Blueprint, property: string) => {
            switch (property) {
                case "uuid":
                    return blueprint.ref.uuid;
                default:
                    return blueprint[property];
            }
        };
        this.filterPredicate = (blueprint: Blueprint, filter: string) => {
            let searchString = filter.trim().toLowerCase();
            return blueprint.ref.uuid.includes(searchString) || blueprint.jsonClass.toLowerCase().includes(searchString);
        };
    }

    connect(): BehaviorSubject<Blueprint[]> {
        return super.connect();
    }
    disconnect() {
    }
}

