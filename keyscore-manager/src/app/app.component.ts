import { Component } from '@angular/core';

@Component({
    selector: 'my-app',
    template: `
        <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
            <a class="navbar-brand" href="#">KEYSCORE</a>
            <!-- TODO: Fix Dropdown not working -->
            <!--<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent"-->
                    <!--aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">-->
                <!--<span class="navbar-toggler-icon"></span>-->
            <!--</button>-->
            <!--<div class="collapse navbar-collapse" id="navbarNavDropdown">-->
                <!--<div class="navbar-nav">-->
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
                <!--</div>-->
            <!--</div>-->
        </nav>
        <div class="container-fluid">
            <div class="row">
                <!-- TODO: Building a sidebar: https://bootstrapious.com/p/bootstrap-sidebar -->
                <div class="col-md-2 col-xs-1 pb-5 mt-3">
                    <div class="list-group">
                        <a class="list-group-item" routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
                        <a class="list-group-item" routerLink="/node" routerLinkActive="active">Nodes</a>
                        <a class="list-group-item" routerLink="/stream" routerLinkActive="active">Streams</a>
                        <a class="list-group-item" routerLink="/filter" routerLinkActive="active">Filters</a>
                    </div>
                </div>
                <div class="col-md-10 col-xs-11 mt-3">
                    <router-outlet></router-outlet>
                </div>
            </div>
        </div>
    `
})

export class AppComponent {
}
