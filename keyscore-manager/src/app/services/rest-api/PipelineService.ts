import {Injectable} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {AppState} from "../../app.component";
import {HttpClient} from "@angular/common/http";
import {AppConfig, selectAppConfig} from "../../app.config";
import {Observable} from "rxjs/index";
import {Ref} from "keyscore-manager-models";

@Injectable({
    providedIn: 'root'
})
export class PipelineService {

    static BASE_URL: string;

    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe(config =>
            PipelineService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url") + "/pipeline");
    }

    runPipeline(blueprintRef:Ref):Observable<any>{
        return this.httpClient.put(`${PipelineService.BASE_URL}/blueprint`,blueprintRef,{
            responseType: 'text'
        });
    }

    stopAllPipelines():Observable<any>{
        return this.httpClient.delete(`${PipelineService.BASE_URL}/blueprint/*`);

    }

    stopPipeline(id:string):Observable<any>{
        console.log("[PipelineService] Stopping pipeline with id:" + id);
        return this.httpClient.put(`${PipelineService.BASE_URL}/blueprint/${id}/stop`,{}, {responseType: 'text'});
    }

    loadAllInstances():Observable<any>{
        return this.httpClient.get(`${PipelineService.BASE_URL}/instance/*`);
    }

    loadInstance(id:string):Observable<any>{
        return this.httpClient.get(`${PipelineService.BASE_URL}/instance/${id}`);
    }
}