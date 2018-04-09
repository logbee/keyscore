import {Component} from '@angular/core';
import {AgentModel, AgentsState, getAgents} from "./agents.model";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs/Observable";
import {LoadAgentsAction} from "./agents.actions";
import {Go} from "../router/router.actions";

@Component({
    selector: 'keyscore-agents',
    template: `
        <div class="row justify-content-center">
            <div class="col-10">
                <div class="card">
                    <div class="card-header d-flex justify-content-between">
                        <span class="font-weight-bold">Agents</span>
                        <button class="btn" (click)="reload()">
                            <img width="24em" src="/assets/images/arrow-reload.svg"/>
                        </button>
                    </div>
                    <div class="card-body">
                    <table class="table table-sm table-hover">
                        <thead>
                        <tr>
                            <th>#</th>
                            <th>Name</th>
                            <th>ID</th>
                            <th>Host</th>
                            <th>&nbsp;</th>
                        </tr>
                        </thead>
                        <tbody>
                            <tr *ngFor="let agent of agents$ | async; let i = index" class="" >
                                <td>{{i}}</td>
                                <td>{{agent.name}}</td>
                                <td>{{agent.id}}</td>
                                <td>{{agent.host}}</td>
                                <td style="text-align: right">
                                    <button class="btn btn-primary" style="padding-bottom: 0; padding-top: 0" (click)="inspect(agent.uid)">
                                        <img style="filter: invert(1);" width="12em" src="/assets/images/chevron-right.svg"/>
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    </div>
                </div>
            </div>
        </div>
    `
})

export class AgentsComponent {

    private agents$: Observable<AgentModel[]>;

    constructor(private store: Store<AgentsState>) {
        this.agents$ = this.store.select(getAgents)
    }

    reload() {
        this.store.dispatch(new LoadAgentsAction())
    }

    inspect(uid: string) {
        this.store.dispatch(new Go({path: ['/agent/' + uid + '/']}))
    }
}
