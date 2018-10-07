import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/index";
import {Blueprint, PipelineBlueprint} from "../../models/blueprints/Blueprint";
import {AppState} from "../../app.component";
import {AppConfig, selectAppConfig} from "../../app.config";
import {select, Store} from "@ngrx/store";
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
            RestCallService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url"));
    }

    loadAllBlueprints(): Observable<Map<string, Blueprint>> {
        return this.httpClient.get<Map<string, Blueprint>>(`${RestCallService.BASE_URL}/resources/blueprint/*`);
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
}