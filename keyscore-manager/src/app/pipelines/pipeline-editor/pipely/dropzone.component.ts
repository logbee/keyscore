import {Component, ElementRef, HostBinding, HostListener, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {WorkspaceComponent} from "./workspace.component";

@Component({
    selector: "dropzone",
    template: `
        <div #dropzone class="dropzone" >

            <draggable [workspace]="workspace"></draggable>
            <draggable [workspace]="workspace"></draggable>
        </div>
    `
})

export class DropzoneComponent implements OnInit, OnDestroy {

    @Input() workspace:WorkspaceComponent;
    @Input() id:string;

    @HostBinding('class.col-6')isCol6:boolean;

    @ViewChild("dropzone") dropzoneElement: ElementRef;


    constructor() {
        this.isCol6 = true;
    }

    public ngOnInit() {

    }

    public ngOnDestroy() {

    }


}
