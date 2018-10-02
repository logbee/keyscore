import {Component, EventEmitter, Input, Output} from "@angular/core";
import "./style/headerbar.style.scss"

@Component({
    selector: "header-bar",
    template: `
        <div id="header-bar" fxLayout="row" fxLayoutAlign="space-between center">
            <span class="title">{{title}}</span>
            <div class="header-button-wrapper" fxLayout="row" fxLayoutAlign="space-around center" fxLayoutGap="10px">
                <button mat-stroked-button *ngIf="this.showManualReload" (click)="reload()">
                    <mat-icon>autorenew</mat-icon>
                </button>
                <button mat-stroked-button *ngIf="this.showSave" (click)="save()">
                    <mat-icon>save</mat-icon>
                </button>
                <button mat-stroked-button *ngIf="this.showRun" (click)="run()">
                    <mat-icon>play_circle_outline</mat-icon>
                </button>
                <button mat-stroked-button color="warn" *ngIf="this.showDelete" (click)="remove()">
                    <mat-icon>delete</mat-icon>
                </button>
            </div>

        </div>
    `
})
export class HeaderBarComponent {
    @Input() public title: string;
    @Input() public showManualReload: boolean;
    @Input() public showSave: boolean;
    @Input() public showRun: boolean;
    @Input() public showDelete: boolean;
    @Output() public onManualReload: EventEmitter<void> = new EventEmitter();
    @Output() public onSave: EventEmitter<void> = new EventEmitter();
    @Output() public onRun: EventEmitter<void> = new EventEmitter();
    @Output() public onDelete: EventEmitter<void> = new EventEmitter();

    private reload() {
        this.onManualReload.emit();
    }

    private save(){
        this.onSave.emit();
    }

    private run(){
        this.onRun.emit();
    }

    private remove(){
        this.onDelete.emit();
    }


}
