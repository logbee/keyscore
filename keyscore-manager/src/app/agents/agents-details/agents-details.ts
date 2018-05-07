import {Component} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {AgentModel, AgentsState, getCurrentAgent} from "../agents.model";
import {Store} from "@ngrx/store";
import {Go} from "../../router/router.actions";
import {TranslateService} from "@ngx-translate/core";

@Component({
    selector: 'agents-details',
    template: `
        <div class="row justify-content-center">
            <div class="col-10">
                <div class="card">
                    <div class="card-header d-flex justify-content-between">
                        <span class="font-weight-bold">{{'AGENTSDETAILS.DETAILEDVIEW' | translate}} {{(agent$ | async).name}}</span>
                        <button class="btn" (click)="reload()">
                            <img width="24em" src="/assets/images/arrow-reload.svg"/>
                        </button>
                    </div>
                    <div class="card-body">
                        <div class="ml-3">
                            <div class="row">
                                <div class="col-lg-6 font-weight-bold">{{'AGENTSDETAILS.AGENTID' | translate}}</div>
                                <div class="col-lg-6">
                                    {{(agent$ | async).id}}
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-lg-6 font-weight-bold">{{'AGENTSDETAILS.AGENTNAME' | translate}}</div>
                                <div class="col-lg-6">
                                    {{(agent$ | async).name}}
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-lg-6 font-weight-bold">{{'AGENTSDETAILS.AGENTHOST' | translate}}</div>
                                <div class="col-lg-6">
                                    {{(agent$ | async).host}}
                                </div>
                            </div>
                        </div>
                        <div class="row ml-3 mt-3 mb-3">
                            <button class="btn" (click)="backToAgentsView()">
                                <img width="24em" src="/assets/images/chevron-left.svg">
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})

export class AgentsDetails {
    private agent$: Observable<AgentModel>;

    constructor(private store: Store<AgentsState>,translate:TranslateService) {
        this.agent$ = this.store.select(getCurrentAgent);
        translate.setDefaultLang('en');
        translate.use('en');
    }

    reload() {
        console.log("reloaded");
        this.agent$ = this.store.select(getCurrentAgent)
    }

    backToAgentsView() {
        this.store.dispatch(new Go({path: ['/agent/']}))
    }
}