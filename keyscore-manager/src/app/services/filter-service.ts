import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AppConfig} from "../app.config";


interface FilterDescriptor {
    name: string;
    displayName: string;
    description: string;
    parameters: ParameterDescriptor[];
}

interface ParameterDescriptor {
    name: string;
    displayName: string;
    kind: string;
    mandatory: boolean;
}

export class FilterBlueprint {
    constructor(public name: string,
                public displayName: string,
                public description: string) {
    }
}


@Injectable()
export class FilterService {
    private readonly serverAddress: string;

    constructor(private config: AppConfig, private http: HttpClient) {
        this.serverAddress = config.getString("keyscore.frontier.base-url")
    }

    getAllFilter(): FilterBlueprint[] {
        let filterList: FilterBlueprint[] = [];

        this.http.get<FilterDescriptor[]>(this.serverAddress + '/descriptors').subscribe(data => {

            for (let filterDescriptor of data) {
                filterList.push(new FilterBlueprint(filterDescriptor.name, filterDescriptor.displayName, filterDescriptor.description))
            }
        });

        return filterList;
    }
}