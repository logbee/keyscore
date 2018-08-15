import {Component, EventEmitter, Input, Output} from "@angular/core";

@Component({
    selector: "header-bar",
    template: `
        <div style="background-color:  #365880; color: white;">
            <div class="p-2 d-flex justify-content-between align-middle">
                <span class="font-weight-bold">{{title}}</span>
                <button *ngIf="this.showManualReload" class="btn" (click)="reload()">
                    <img width="24em" src="/assets/images/arrow-reload.svg"/>
                </button>
            </div>
        </div>
    `
})
export class HeaderBarComponent {
    @Input() public title: string;
    @Input() public showManualReload: boolean;
    @Output() public onManualReload = new EventEmitter<any>();

    private reload() {
        this.onManualReload.emit({});
    }
}
