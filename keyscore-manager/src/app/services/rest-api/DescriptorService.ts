import {Injectable} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {AppState} from "../../app.component";
import {HttpClient} from "@angular/common/http";
import {AppConfig, selectAppConfig} from "../../app.config";
import {Observable} from "rxjs/index";
import {BlueprintService} from "./BlueprintService";
import {StringTMap} from "../../common/object-maps";
import {Descriptor} from "../../models/descriptors/Descriptor";

@Injectable({
    providedIn: 'root'
})
export class DescriptorService {

    static BASE_URL: string;

    constructor(private httpClient: HttpClient, private store: Store<AppState>) {
        this.store.pipe(select(selectAppConfig)).subscribe(config =>
            DescriptorService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url") + "/resources/descriptor");
    }

    getAllDescriptors(): Observable<StringTMap<Descriptor>> {
        return this.httpClient.get<StringTMap<Descriptor>>(`${DescriptorService.BASE_URL}/*`);
    }

    getDescriptorById(uuid: string): Observable<Descriptor> {
        return this.httpClient.get<Descriptor>(`${DescriptorService.BASE_URL}/${uuid}`);
    }
}