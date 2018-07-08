import {Component} from "@angular/core";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {Go} from "../router/router.actions";
import {LoadAgentsAction} from "./agents.actions";
import {AgentModel, AgentsState, getAgents} from "./agents.model";

@Component({
    selector: "keyscore-agents",
    template: `
        <header-bar
            title="Agents"
            [showManualReload]="true"
            (onManualReload)="reload()">
        </header-bar>
        <table class="table table-sm table-hover">
            <thead>
            <tr>
                <th>{{'AGENTSCOMPONENT.NUMBER' | translate}}</th>
                <th>{{'AGENTSCOMPONENT.NAME' | translate}}</th>
                <th>{{'AGENTSCOMPONENT.ID' | translate}}</th>
                <th>{{'AGENTSCOMPONENT.HOST' | translate}}</th>
                <th>&nbsp;</th>
            </tr>
            </thead>
            <tbody>
            <tr *ngFor="let agent of agents$ | async; let i = index" class="">
                <td>{{i}}</td>
                <td>{{agent.name}}</td>
                <td>{{agent.id}}</td>
                <td>{{agent.host}}</td>
                <td style="text-align: right">
                    <button class="btn btn-primary" style="padding-bottom: 0; padding-top: 0"
                            (click)="inspect(agent.id)">
                        <img style="filter: invert(1);" width="12em"
                             src="/assets/images/chevron-right.svg"/>
                    </button>
                </td>
            </tr>
            </tbody>
        </table>
    `
})

export class AgentsComponent {

    private agents$: Observable<AgentModel[]>;

    constructor(private store: Store<AgentsState>) {
        this.agents$ = this.store.select(getAgents);
    }

    public reload() {
        this.store.dispatch(new LoadAgentsAction());
    }

    public inspect(id: string) {
        this.store.dispatch(new Go({path: ["/agent/" + id + "/"]}));
    }
}
