import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/index";
import {Blueprint, PipelineBlueprint} from "@keyscore-manager-models";
import {AppState} from "../../app.component";
import {AppConfig, selectAppConfig} from "../../app.config";
import {select, Store} from "@ngrx/store";
import {StringTMap} from "../../common/object-maps";

@Injectable({
    providedIn: 'root'
})
export class BlueprintService {

    static BASE_URL: string;

    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe(config =>
            BlueprintService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url") + "/resources/blueprint");
    }

    //Blueprints
    loadAllPipelineBlueprints(): Observable<StringTMap<PipelineBlueprint>> {
        return this.httpClient.get<StringTMap<PipelineBlueprint>>(`${BlueprintService.BASE_URL}/pipeline/*`)
    }


    loadAllBlueprints(): Observable<Blueprint[]> {
        return this.httpClient.get<Blueprint[]>(`${BlueprintService.BASE_URL}/*`);
    }

    getBlueprint(id: string): Observable<Blueprint> {
        return this.httpClient.get<Blueprint>(`${BlueprintService.BASE_URL}/${id}`);
    }

    putBlueprint(blueprint: Blueprint): Observable<any> {
        return this.httpClient.put(`${BlueprintService.BASE_URL}/${blueprint.ref.uuid}`, blueprint, {responseType: 'text'});
    }

    //PipelineBlueprints
    getPipelineBlueprint(id: string): Observable<PipelineBlueprint> {
        return this.httpClient.get<PipelineBlueprint>(`${BlueprintService.BASE_URL}/pipeline/${id}`);
    }

    putPipelineBlueprint(pipelineBlueprint: PipelineBlueprint): Observable<any> {
        return this.httpClient.put(`${BlueprintService.BASE_URL}/pipeline/${pipelineBlueprint.ref.uuid}`, pipelineBlueprint, {responseType: 'text'});
    }
}