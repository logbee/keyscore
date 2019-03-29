import {KeycloakAuthGuard, KeycloakService} from "keycloak-angular";
import {ActivatedRouteSnapshot, Router, RouterStateSnapshot} from "@angular/router";

export class AppConcreteKeycloakAuthguard extends KeycloakAuthGuard {


    constructor(protected router: Router, protected keycloakService: KeycloakService) {
        super(router, keycloakService);
    }

    isAccessAllowed(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
        return new Promise(async (resolve, reject) => {
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
        });
    }
}