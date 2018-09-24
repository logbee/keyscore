import {DataSource} from "@angular/cdk/collections";
import {Blueprint} from "../models/blueprints/Blueprint";
import {Observable} from "rxjs/index";

export class BlueprintDataSource implements DataSource<Blueprint> {

    connect(): Observable<Blueprint[]> {
        return undefined;
    }

    disconnect() {}


}