import {
    AfterViewInit, Component, ElementRef, HostBinding, HostListener, ViewChild,
    ViewContainerRef
} from "@angular/core";
import {DropzoneSubcomponent} from "./dropzone-subcomponent";

@Component({
    selector: "workspace-dropzone",
    template: `

        <div #workspaceScrollContainer class="workspace-scroll-wrapper">
            <div #dropzone [class]="'dropzone-workspace'">
                <ng-template #draggableContainer></ng-template>
            </div>

        </div>

    `
})

export class WorkspaceDropzoneSubcomponent implements DropzoneSubcomponent, AfterViewInit {

    @ViewChild("draggableContainer", {read: ViewContainerRef}) draggableContainer: ViewContainerRef;

    @ViewChild("dropzone") dropzoneElement: ElementRef;

    @ViewChild("workspaceScrollContainer", {read: ElementRef}) workspaceScrollContainer: ElementRef;



    isDroppable: boolean;
    @HostBinding('class.move-cursor')isSwiping: boolean = false;
    lastClientX: number = 0;

    @HostListener('mousedown', ['$event'])
    startWorkspaceSwipe(event: MouseEvent) {
        this.isSwiping = true;
        this.lastClientX = event.clientX;
    }

    @HostListener('document:mousemove', ['$event'])
    swipeWorkspace(event: MouseEvent) {
        if (this.isSwiping) {
            const deltaX = event.clientX - this.lastClientX;
            this.lastClientX = event.clientX;
            this.workspaceScrollContainer.nativeElement.scrollLeft -= deltaX;
        }
    }

    @HostListener('document:mouseup', ['$event'])
    stopSwipeWorkspace(event: MouseEvent) {
        this.isSwiping = false;
    }

    ngAfterViewInit() {
        this.centerScrollbar();
    }

    private centerScrollbar() {
        const scrollWidth = this.workspaceScrollContainer.nativeElement.scrollWidth;
        const elementWidth = this.workspaceScrollContainer.nativeElement.offsetWidth;


        /*const scrollHeight = this.workspaceScrollContainer.nativeElement.scrollHeight;
        const elementHeight = this.workspaceScrollContainer.nativeElement.offsetHeight;*/

        this.workspaceScrollContainer.nativeElement.scrollLeft = scrollWidth / 2 - elementWidth / 2;
        //this.workspaceScrollContainer.nativeElement.scrollTop = scrollHeight/2 - elementHeight/2;
    }

}