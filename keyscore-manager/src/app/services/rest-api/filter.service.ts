import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AppState} from "../../app.component";
import {AppConfig, selectAppConfig} from "../../app.config";
import {select, Store} from "@ngrx/store";
import {Observable} from "rxjs/index";
import {Blueprint} from "../../models/blueprints/Blueprint";


@Injectable({
    providedIn: 'root'
})
export class FilterService {
    static BASE_URL: string;
    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe( config =>
            FilterService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url")
        )
    }
    loadAllBlueprints(): Observable<Map<string, Blueprint>> {
        return this.httpClient.get<Map<string, Blueprint>>(`${FilterService.BASE_URL}/resources/blueprint/*`);
    }
}
