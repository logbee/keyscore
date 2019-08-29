import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/index";
import {Agent} from "@keyscore-manager-models";
import {AppState} from "../../app.component";
import {AppConfig, selectAppConfig} from "../../app.config";
import {select, Store} from "@ngrx/store";

@Injectable({
    providedIn: 'root'
})
export class AgentService {

    static BASE_URL: string;

    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe(config =>
            AgentService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url") + "/agent");
    }

    loadAgents(): Observable<Agent[]> {
        return this.httpClient.get<Agent[]>(`${AgentService.BASE_URL}/`)
    }

    deleteAgent(id:string): Observable<any>{
        return this.httpClient.delete<any>(`${AgentService.BASE_URL}/${id}`)
    }

}