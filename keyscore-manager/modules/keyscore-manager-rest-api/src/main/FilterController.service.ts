import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {AppState} from "../../../../src/app/app.component";
import {AppConfig, selectAppConfig} from "../../../../src/app/app.config";
import {select, Store} from "@ngrx/store";
import {Observable} from "rxjs/index";
import {Configuration, Dataset, ResourceInstanceState} from "@keyscore-manager-models";


@Injectable({
    providedIn: 'root'
})
export class FilterControllerService {
    static BASE_URL: string;

    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe(config =>
            FilterControllerService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url") + "/filter");
    }


    getState(uuid: string): Observable<ResourceInstanceState> {
        return this.httpClient.get<ResourceInstanceState>(`${FilterControllerService.BASE_URL}/${uuid}/state`);
    }

    pauseFilter(uuid: string, pause: boolean): Observable<any> {
        return this.httpClient.post(`${FilterControllerService.BASE_URL}/${uuid}/pause?value=${pause}`, {}, {
            headers: new HttpHeaders().set("Content-Type", "application/json"),
            responseType: "json"
        });
    }

    drainFilter(uuid: string, drain: boolean): Observable<any> {
        return this.httpClient.post(`${FilterControllerService.BASE_URL}/${uuid}/drain?value=${drain}`, {}, {
            headers: new HttpHeaders().set("Content-Type", "application/json"),
            responseType: "json"
        })
    }

    insertDatasets(uuid: string, datasets: Dataset[]): Observable<any> {
        return this.httpClient.put(`${FilterControllerService.BASE_URL}/${uuid}/insert?where=before`, datasets, {
            headers: new HttpHeaders().set("Content-Type", "application/json"),
            responseType: "json"
        });
    }

    extractDatasets(uuid: string, amount: number, where: string): Observable<Dataset[]> {
        return this.httpClient.get<Dataset[]>(`${FilterControllerService.BASE_URL}/${uuid}/extract?value=` + amount + "&where=" + where)
    }

    updateConfig(configuration: Configuration, blueprintId: string): Observable<any> {
        return this.httpClient.put(`${FilterControllerService.BASE_URL}/${blueprintId}/configurations`, configuration, {
            headers: new HttpHeaders().set("Content-Type", "application/json"),
            responseType: "json"
        })
    }
}
