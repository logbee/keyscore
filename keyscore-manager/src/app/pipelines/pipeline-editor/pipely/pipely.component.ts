import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {Subject} from "rxjs/index";

import "./style/pipely-style.scss";
import {PipelyPipelineConfiguration} from "./models/pipeline-configuration.model";

@Component({
    selector: "pipely-workspace",
    template:
            `
        <div class="pipely-wrapper">
                <workspace [pipeline]="pipeline"></workspace>
        </div>

    `
})

export class PipelyComponent implements OnInit, OnDestroy {
    @Input() pipeline: PipelyPipelineConfiguration;
    private alive$: Subject<void> = new Subject();

    public ngOnDestroy() {
        this.alive$.next();
    }

    public ngOnInit() {


    }

}
