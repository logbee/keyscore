import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs/Rx';

@Injectable()
export class AppConfig {

    private configuration: any = null;

    constructor(private http: HttpClient) {
    }

    public getString(key: string): string {
        return <string>this.resolveValue(key.split('.'), this.configuration)
    }

    public load() {
        return new Promise((resolve, reject) => {
            this.http.get('application.conf')
                .catch(error => {
                    console.error("Failed to load application.conf");
                    reject(true);
                    return Observable.throw(error || 'Server error');
                })
                .subscribe((data) => {
                    this.configuration = data;
                    resolve(true)
                })
        });
    }

    private resolveValue(keys: Array<string>, config: Object): any {
        if (keys.length == 1) {
            return config[keys[0]]
        }
        else {
            return this.resolveValue(keys, config[keys.shift()])
        }
    }
}