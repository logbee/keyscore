import {Component, OnDestroy} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs";
import {Go} from "../../router/router.actions";
import {AgentsState, getCurrentAgent} from "../agents.reducer";
import {RemoveCurrentAgentAction} from "../agents.actions";
import {skipWhile, take, takeUntil} from "rxjs/operators";
import {Subject} from "rxjs/internal/Subject";
import {isSpinnerShowing} from "../../common/loading/loading.reducer";
import {Agent} from "@keyscore-manager-models";

@Component({
    selector: "agents-details",
    template: `
        <div class="row justify-content-center">
            <div class="col-10">
                <div class="card">
                    <div class="card-header d-flex justify-content-between">
                        <span class="font-weight-bold" *ngIf="!(isLoading$ | async)">
                            {{'AGENTSDETAILS.DETAILEDVIEW' | translate}} {{(agent$ | async)?.name}}
                        </span>
                        <div class="row">
                            <div class="col-1 mr-5">
                                <button class="btn" (click)="reload()">
                                    <img width="24em" src="/assets/images/arrow-reload.svg"/>
                                </button>
                            </div>
                            <div class="col-1 mr-5">
                                <button class="btn" (click)="deleteAgent()">
                                    <img width="24em" src="/assets/images/ic_delete_dark_24px.svg"/>
                                </button>
                            </div>
                            <loading class="col-1" *ngIf="isLoading$ | async"></loading>
                        </div>
                    </div>
                    <div class="card-body" *ngIf="!(isLoading$ | async)">
                        <div class="ml-3">
                            <div class="row">
                                <div class="col-lg-6 font-weight-bold">{{'AGENTSDETAILS.AGENTID' | translate}}</div>
                                <div class="col-lg-6">
                                    {{(agent$ | async)?.id}}
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-lg-6 font-weight-bold">{{'AGENTSDETAILS.AGENTNAME' | translate}}</div>
                                <div class="col-lg-6">
                                    {{(agent$ | async)?.name}}
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-lg-6 font-weight-bold">{{'AGENTSDETAILS.AGENTHOST' | translate}}</div>
                                <div class="col-lg-6">
                                    {{(agent$ | async)?.host}}
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

export class AgentsDetails implements OnDestroy {
    private agent$: Observable<Agent>;
    private agentId: string;
    private isAlive$: Subject<void> = new Subject();
    private isLoading$: Observable<boolean>;

    constructor(private store: Store<AgentsState>, translate: TranslateService) {
        this.isLoading$ = this.store.select(isSpinnerShowing);

        this.isLoading$.pipe(takeUntil(this.isAlive$), skipWhile(isLoading => isLoading), take(1))
            .subscribe(_ => {
                this.agent$ = this.store.select(getCurrentAgent);
                this.agent$.pipe(takeUntil(this.isAlive$), take(1)).subscribe(agent => this.agentId = agent.id);
            });



        translate.setDefaultLang("en");
        translate.use("en");
    }

    public reload() {
        this.agent$ = this.store.select(getCurrentAgent);
    }

    public deleteAgent() {
        this.store.dispatch(new RemoveCurrentAgentAction(this.agentId));
    }

    public backToAgentsView() {
        this.store.dispatch(new Go({path: ["/agent/"]}));
    }

    ngOnDestroy() {
        this.isAlive$.next();
    }
}
