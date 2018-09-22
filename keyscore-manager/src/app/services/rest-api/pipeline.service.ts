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
export class PipelineService {

    static BASE_URL: string;

    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe(config =>
            PipelineService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url") + "/resources")
    }

    getPipelineBlueprint(id: string): Observable<PipelineBlueprint> {
        return this.httpClient.get<PipelineBlueprint>(`${PipelineService.BASE_URL}/blueprint/pipeline/${id}`);
    }

    getBlueprint(id:string): Observable<Blueprint> {
        return this.httpClient.get<Blueprint>(`${PipelineService.BASE_URL}/blueprint/${id}`);
    }

    getConfiguration(id:string): Observable<Configuration>{
        return this.httpClient.get<Configuration>(`${PipelineService.BASE_URL}/configuration/${id}`);
    }

    getAllDescriptors():Observable<StringTMap<Descriptor>>{
        return this.httpClient.get<StringTMap<Descriptor>>(`${PipelineService.BASE_URL}/descriptor/*`);
    }

}