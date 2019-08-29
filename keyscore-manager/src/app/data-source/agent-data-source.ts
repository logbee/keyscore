import {MatTableDataSource} from "@angular/material";
import {BehaviorSubject, Observable} from "rxjs/index";
import {Agent} from "@keyscore-manager-models";

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