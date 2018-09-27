import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/index";
import {Blueprint, PipelineBlueprint} from "../../models/blueprints/Blueprint";
import {AppState} from "../../app.component";
import {AppConfig, selectAppConfig} from "../../app.config";
import {Store,select} from "@ngrx/store";
import {Configuration} from "../../models/common/Configuration";
import {Descriptor} from "../../models/descriptors/Descriptor";
import {StringTMap} from "../../common/object-maps";

@Injectable({
    providedIn: 'root'
})
export class RestCallService {

    static BASE_URL: string;

    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe(config =>
            RestCallService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url") + "/resources")
    }

    getPipelineBlueprint(id: string): Observable<PipelineBlueprint> {
        return this.httpClient.get<PipelineBlueprint>(`${RestCallService.BASE_URL}/blueprint/pipeline/${id}`);
    }

    getBlueprint(id:string): Observable<Blueprint> {
        return this.httpClient.get<Blueprint>(`${RestCallService.BASE_URL}/blueprint/${id}`);
    }

    getConfiguration(id:string): Observable<Configuration>{
        return this.httpClient.get<Configuration>(`${RestCallService.BASE_URL}/configuration/${id}`);
    }

    getAllConfigurations(): Observable<Configuration[]> {
        return this.httpClient.get<Configuration[]>(`${RestCallService.BASE_URL}/configuration/*`);
    }

    getAllDescriptors():Observable<StringTMap<Descriptor>>{
        return this.httpClient.get<StringTMap<Descriptor>>(`${RestCallService.BASE_URL}/descriptor/*`);
    }

    updatePipelineBlueprint(pipelineBlueprint:PipelineBlueprint):Observable<any>{
        return this.httpClient.post(`${RestCallService.BASE_URL}/blueprints/pipeline/${pipelineBlueprint.ref.uuid}`,pipelineBlueprint);
    }

    createPipelineBlueprint(pipelineBlueprint:PipelineBlueprint):Observable<any>{
        return this.httpClient.put(`${RestCallService.BASE_URL}/blueprints/pipeline/${pipelineBlueprint.ref.uuid}`,pipelineBlueprint);
    }

}