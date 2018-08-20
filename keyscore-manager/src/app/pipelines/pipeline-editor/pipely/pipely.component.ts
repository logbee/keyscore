import {Component, OnDestroy, OnInit} from "@angular/core";
import {Subject} from "rxjs/index";

import "./style/pipely-style.css";

@Component({
    selector: "pipely-workspace",
    template:
            `
        <div class="pipely-wrapper">
            <div class="row">
                <workspace class="col-12 p-0"></workspace>
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
