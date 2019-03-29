import {Injectable} from "@angular/core";
import {KeycloakAuthGuard, KeycloakService} from "keycloak-angular";
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from "@angular/router";
import {select, Store} from "@ngrx/store";
import {AppState} from "./app.component";
import {selectAppConfig} from "./app.config";
import {take} from "rxjs/operators";
import {AppConcreteKeycloakAuthguard} from "./app.concrete-keycloak-authguard";

@Injectable()
export class AppAuthGuard implements CanActivate {

    private keycloakAuthGuard:AppConcreteKeycloakAuthguard;

    constructor(protected router: Router, protected keycloakService: KeycloakService, private store: Store<AppState>) {
        let isAuth: boolean = false;
        this.store.pipe(select(selectAppConfig), take(1)).subscribe(conf => {
                if (conf) {
                    isAuth = conf.getBoolean("keyscore.keycloak.active")
                }
            }
        );
        if(isAuth){
            this.keycloakAuthGuard = new AppConcreteKeycloakAuthguard(this.router,this.keycloakService)
        }
    }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
        if(this.keycloakAuthGuard) {
            return this.keycloakAuthGuard.canActivate(route, state);
        } else {
            return new Promise<boolean>((resolve,reject) => resolve(true));
        }
    }

}