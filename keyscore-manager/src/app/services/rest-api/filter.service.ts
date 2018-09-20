/*
import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Store} from "@ngrx/store";
import {select} from "@ngrx/core";
import {AppState} from "../../app.component";
import {AppConfig, selectAppConfig} from "../../app.config";

@Injectable({
    providedIn: 'root'
})
export class FilterService {
    static BASE_URL: string;
    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe( config =>
            FilterService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url") + "/filter"
        )
    }

}*/
