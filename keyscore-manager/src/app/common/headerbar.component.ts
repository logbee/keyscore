import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {FilterConfiguration} from "../models/filter-model/FilterConfiguration";
import {FilterDescriptor} from "../models/filter-model/FilterDescriptor";

@Component({
    selector: "header-bar",
    template: `
        <div id="header-bar">
            <div class="p-2 d-flex justify-content-between align-middle">
                <span class="title">{{title}}</span>
                <button *ngIf="this.showManualReload" class="reload btn btn-light btn-sm" (click)="reload()">
                    <img width="24em" src="/assets/images/arrow-reload.svg"/>
                </button>
            </div>
        </div>
    `
})
export class HeaderBarComponent implements OnInit{
    @Input() public title: string;
    @Input() public showManualReload: boolean;
    @Input() public filter?: FilterDescriptor;
    @Output() public onManualReload = new EventEmitter<any>();

    private reload() {
        this.onManualReload.emit({});
    }

    ngOnInit(): void {
        if (this.filter != null) {
            this.title = this.filter.displayName;
        }
    }
}
