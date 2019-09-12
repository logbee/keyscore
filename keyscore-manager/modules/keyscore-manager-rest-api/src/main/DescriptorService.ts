import {Injectable} from "@angular/core";
import {select, Store} from "@ngrx/store";
import {AppState} from "@/app/app.component";
import {HttpClient} from "@angular/common/http";
import {AppConfig, selectAppConfig} from "@/app/app.config";
import {Observable} from "rxjs";
import {StringTMap} from "@/app/common/object-maps";
import {map} from "rxjs/operators";
import {DeserializerService} from "@keyscore-manager-rest-api/src/main/deserializer.service";
import {Descriptor} from "@/../modules/keyscore-manager-models/src/main/descriptors/Descriptor";

@Injectable({
    providedIn: 'root'
})
export class DescriptorService {

    static BASE_URL: string;

    constructor(private httpClient: HttpClient, private store: Store<AppState>,private deserializer:DeserializerService) {
        this.store.pipe(select(selectAppConfig)).subscribe(config =>
            DescriptorService.BASE_URL = (config as AppConfig).getString("keyscore.frontier.base-url") + "/resources/descriptor");
    }

    getAllDescriptors(): Observable<Descriptor[]> {
        return this.httpClient.get<StringTMap<Descriptor>>(`${DescriptorService.BASE_URL}/*`)
            .pipe(map(descriptorMap => Object.values(descriptorMap)));
    }

    getDescriptorById(uuid: string): Observable<Descriptor> {
        return this.httpClient.get<Descriptor>(`${DescriptorService.BASE_URL}/${uuid}`);
    }

}