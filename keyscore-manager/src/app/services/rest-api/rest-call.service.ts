import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs/index";
import {Blueprint, PipelineBlueprint} from "../../models/blueprints/Blueprint";
import {AppState} from "../../app.component";
import {AppConfig, selectAppConfig} from "../../app.config";
import {Store, select} from "@ngrx/store";
import {Configuration} from "../../models/common/Configuration";
import {Descriptor} from "../../models/descriptors/Descriptor";
import {StringTMap} from "../../common/object-maps";
import {ResourceInstanceState} from "../../models/filter-model/ResourceInstanceState";
import {Dataset} from "../../models/dataset/Dataset";

@Injectable({
    providedIn: 'root'
})
export class RestCallService {

    static BASE_URL: string;

    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe(config =>
            RestCallService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url"));
    }

    //Blueprints
    getPipelineBlueprint(id: string): Observable<PipelineBlueprint> {
        return this.httpClient.get<PipelineBlueprint>(`${RestCallService.BASE_URL}/resources/blueprint/pipeline/${id}`);
    }

    getBlueprint(id: string): Observable<Blueprint> {
        return this.httpClient.get<Blueprint>(`${RestCallService.BASE_URL}/resources/blueprint/${id}`);
    }

    putPipelineBlueprint(pipelineBlueprint: PipelineBlueprint): Observable<any> {
        return this.httpClient.put(`${RestCallService.BASE_URL}/resources/blueprint/pipeline/${pipelineBlueprint.ref.uuid}`, pipelineBlueprint, {responseType: 'text'});
    }

    putBlueprint(blueprint: Blueprint): Observable<any> {
        return this.httpClient.put(`${RestCallService.BASE_URL}/resources/blueprint/${blueprint.ref.uuid}`, blueprint, {responseType: 'text'});
    }

    //Configurations
    getConfiguration(id: string): Observable<Configuration> {
        return this.httpClient.get<Configuration>(`${RestCallService.BASE_URL}/resources/configuration/${id}`);
    }

    getAllConfigurations(): Observable<Configuration[]> {
        return this.httpClient.get<Configuration[]>(`${RestCallService.BASE_URL}/resources/configuration/*`);
    }

    putConfiguration(configuration: Configuration): Observable<any> {
        return this.httpClient.put(`${RestCallService.BASE_URL}/resources/configuration/${configuration.ref.uuid}`, configuration, {responseType: 'text'});
    }

    //Descriptors
    getAllDescriptors(): Observable<StringTMap<Descriptor>> {
        return this.httpClient.get<StringTMap<Descriptor>>(`${RestCallService.BASE_URL}/resources/descriptor/*`);
    }

    getDescriptorById(uuid: string): Observable<Descriptor> {
        return this.httpClient.get<Descriptor>(`${RestCallService.BASE_URL}/resources/descriptor/${uuid}`)
    }

    //Resources/Filter
    getResourceState(uuid: string): Observable<ResourceInstanceState> {
        return this.httpClient.get<ResourceInstanceState>(`${RestCallService.BASE_URL}/filter/${uuid}/state`);
    }

    pauseFilter(uuid: string, pause: boolean): Observable<any> {
        return this.httpClient.post(`${RestCallService.BASE_URL}/filter/${uuid}/pause?value=${pause}`, {}, {
            headers: new HttpHeaders().set("Content-Type", "application/json"),
            responseType: "json"
        });
    }

    drainFilter(uuid: string, drain: boolean): Observable<any> {
        return this.httpClient.post(`${RestCallService.BASE_URL}/filter/${uuid}/drain?value=${drain}`, {}, {
            headers: new HttpHeaders().set("Content-Type", "application/json"),
            responseType: "json"
        })
    }

    insertDatasets(uuid: string, datasets: Dataset[]): Observable<any> {
        return this.httpClient.put(`${RestCallService.BASE_URL}/filter/${uuid}/insert?where=before`, datasets, {
            headers: new HttpHeaders().set("Content-Type", "application/json"),
            responseType: "json"
        });
    }

    extractDatasets(uuid: string): Observable<Dataset[]> {
        return this.httpClient.get<Dataset[]>(`${RestCallService.BASE_URL}/filter/${uuid}/extract?value=10`)
    }

    updateConfig(configuration: Configuration): Observable<any> {
        return this.httpClient.put(`${RestCallService.BASE_URL}/filter/${configuration.ref.uuid}/config`, configuration, {
            headers: new HttpHeaders().set("Content-Type", "application/json"),
            responseType: "json"
        })
    }
}