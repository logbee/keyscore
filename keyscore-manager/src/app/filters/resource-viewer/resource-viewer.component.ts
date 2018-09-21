import {Component, OnInit} from "@angular/core";
import {Store} from "@ngrx/store";
import {TranslateService} from "@ngx-translate/core";
import {Blueprint} from "../../models/blueprints/Blueprint";
import {Observable} from "rxjs/index";
import {selectBlueprints} from "./resource-viewer.reducer";

@Component({
    selector: "resource-viewer",
    template: `
        <header-bar
                [showManualReload]="false"
                [title]="title">
        </header-bar>
        <div fxFlexFill="" fxLayout="row" fxLayoutGap="15px" class="table-wrapper">
            <div *ngFor="let blueprint of blueprints$ | async">{{blueprint.jsonClass}}</div>
        </div>
    `
})
export class ResourceViewerComponent implements OnInit{
    private title: string = "Resources Overview";
    private blueprints$: Observable<Blueprint[]>;
    private blueprint : Blueprint[];
    constructor(private store: Store<any>, private translate: TranslateService) {
    }

    ngOnInit(): void {
        this.blueprints$ = this.store.select(selectBlueprints);

    }

}