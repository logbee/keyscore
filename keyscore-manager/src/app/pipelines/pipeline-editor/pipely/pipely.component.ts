import {Component, OnDestroy, OnInit} from "@angular/core";
import {Subject} from "rxjs/index";
import {Sortable} from "@shopify/draggable";
import {Plugins} from "@shopify/draggable";
import {Draggable} from "@shopify/draggable";
import {Droppable} from "@shopify/draggable";

import "./style/pipely-style.css";

@Component({
    selector: "pipely-workspace",
    template:
            `
        <div class="pipely-wrapper">
            <div class="row">
                <h1>Pipely</h1>
            </div>
            <div class="row">
                <workspace class="col-12"></workspace>
            </div>
        </div>

    `
})

export class PipelyComponent implements OnInit, OnDestroy {
    private alive$: Subject<void> = new Subject();

    public ngOnDestroy() {
        this.alive$.next();
    }

    public ngOnInit() {


    }

}
