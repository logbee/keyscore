import {Injectable} from "@angular/core";
import {KeycloakAuthGuard, KeycloakService} from "keycloak-angular";
import {ActivatedRouteSnapshot, Router, RouterStateSnapshot} from "@angular/router";
import {select, Store} from "@ngrx/store";
import {AppState} from "./app.component";
import {selectAppConfig} from "./app.config";
import {take} from "rxjs/operators";

@Injectable()
export class AppAuthGuard extends KeycloakAuthGuard {
    constructor(protected router: Router, protected keycloakService: KeycloakService, private store: Store<AppState>) {
        super(router, keycloakService);
    }

    isAccessAllowed(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
        console.log("APP AUTHGUARD ISACCESSALLOWED");
        return new Promise(async (resolve, reject) => {
            let isAuth: boolean = false;
            this.store.pipe(select(selectAppConfig), take(1)).subscribe(conf => {
                    if (conf) {
                        isAuth = conf.getBoolean("keyscore.keycloak.active")
                    }
                }
            );
            if (isAuth) {
                console.log("IsAUTH is TRUE");
                if (!this.authenticated) {
                    this.keycloakService.login();
                    return;
                }

                const requiredRoles = route.data.roles;
                if (!requiredRoles || requiredRoles.length === 0) {
                    return resolve(true);
                } else {
                    if (!this.roles || this.roles.length === 0) {
                        resolve(false);
                    }
                    let granted: boolean = false;
                    for (const requiredRole of requiredRoles) {
                        if (this.roles.indexOf(requiredRole) > -1) {
                            granted = true;
                            break;
                        }
                    }
                    resolve(granted);
                }
            } else {
                console.log("isAuth is FALSE");
                resolve(true);
            }
        });
    }
}