import { Component } from '@angular/core';

@Component({
    selector: 'my-app',
    template: `
        <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
            <a class="navbar-brand" href="#">KEYSCORE</a>
            <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent"
                    aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNavDropdown">
                <div class="navbar-nav">
                    <div class="nav-item">
                        <a class="nav-link" routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
                    </div>
                    <div class="nav-item">
                        <a class="nav-link" routerLink="/node" routerLinkActive="active">Nodes</a>
                    </div>
                    <div class="nav-item">
                        <a class="nav-link" routerLink="/stream" routerLinkActive="active">Streams</a>
                    </div>
                    <div class="nav-item">
                        <a class="nav-link" routerLink="/filter" routerLinkActive="active">Filters</a>
                    </div>
                    <!-- TODO: Fix Dropdown not working -->
                    <!--<div class="nav-item dropdown">-->
                        <!--<a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button"-->
                           <!--data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">-->
                            <!--Dropdown-->
                        <!--</a>-->
                        <!--<div class="dropdown-menu" aria-labelledby="navbarDropdown">-->
                            <!--<a class="dropdown-item" href="#">Action</a>-->
                            <!--<a class="dropdown-item" href="#">Another action</a>-->
                            <!--<div class="dropdown-divider"></div>-->
                            <!--<a class="dropdown-item" href="#">Something else here</a>-->
                        <!--</div>-->
                    <!--</div>-->
                </div>
            </div>
        </nav>
        <div class="container-fluid">
            <router-outlet></router-outlet>
        </div>
    `
})

export class AppComponent {
}
