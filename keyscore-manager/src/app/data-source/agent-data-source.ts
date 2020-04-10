import { MatTableDataSource } from "@angular/material/table";
import {BehaviorSubject, Observable} from "rxjs/index";
import {Agent} from "@/../modules/keyscore-manager-models/src/main/common/Agent";

export class AgentDataSource extends MatTableDataSource<Agent> {

    constructor(agents$: Observable<Agent[]>) {
        super();
        agents$.subscribe(agents => {
            this.data = agents;
        })
    }

    connect(): BehaviorSubject<Agent[]> {
        return super.connect();
    }

    disconnect() {
    }
}