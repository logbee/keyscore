import {Injectable} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {AppState} from "@/app/app.component";
import {HttpClient} from "@angular/common/http";
import {AppConfig, selectAppConfig} from "@/app/app.config";
import {Observable} from "rxjs";
import {Configuration} from "@/../modules/keyscore-manager-models/src/main/common/Configuration";

@Injectable({
    providedIn: 'root'
})
export class ConfigurationService {

    static BASE_URL: string;

    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe(config =>
            ConfigurationService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url") + "/resources/configuration");
    }

    getConfiguration(id: string): Observable<Configuration> {
        return this.httpClient.get<Configuration>(`${ConfigurationService.BASE_URL}/${id}`);
    }

    getAllConfigurations(): Observable<Configuration[]> {
        return this.httpClient.get<Configuration[]>(`${ConfigurationService.BASE_URL}/*`);
    }

    putConfiguration(configuration: Configuration): Observable<any> {
        return this.httpClient.put(`${ConfigurationService.BASE_URL}/${configuration.ref.uuid}`, configuration, {responseType: 'text'});
    }
}