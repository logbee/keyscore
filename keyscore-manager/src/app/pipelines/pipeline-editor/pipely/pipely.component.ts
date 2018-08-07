import {Component, OnDestroy, OnInit} from "@angular/core";
import {Subject} from "rxjs/index";

@Component({
    selector: "pipely-workspace",
    template: `
        <div class="col-12">
            <h1>Pipely</h1>
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
