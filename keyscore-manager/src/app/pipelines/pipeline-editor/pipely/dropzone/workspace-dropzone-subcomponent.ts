import {
    AfterViewInit, Component, ElementRef, HostBinding, HostListener, ViewChild,
    ViewContainerRef
} from "@angular/core";
import {DropzoneSubcomponent} from "./dropzone-subcomponent";
import {DraggableModel} from "../models/draggable.model";
import {Draggable} from "../models/contract";

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

    private initialWorkspaceWidth;


    isDroppable: boolean;
    @HostBinding('class.move-cursor') isSwiping: boolean = false;
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

    @HostListener('mouseup', ['$event'])
    stopSwipeWorkspace(event: MouseEvent) {
        this.isSwiping = false;
    }

    ngAfterViewInit() {
        this.centerScrollbar();
        this.initialWorkspaceWidth = this.dropzoneElement.nativeElement.scrollWidth;
    }

    resizeWorkspaceOnDrop(draggables: Draggable[]): number {
        console.log("resizeWorkspace");
        const workspacePadding = 200;
        const workspaceWidth = this.dropzoneElement.nativeElement.scrollWidth;
        const wrapperWidth = this.workspaceScrollContainer.nativeElement.offsetWidth;

        let mostLeftPosition: number = this.getMostLeftPosition(draggables, workspaceWidth);

        if (mostLeftPosition <= workspacePadding) {
            return this.growLeft(workspacePadding, mostLeftPosition, workspaceWidth, wrapperWidth);
        }

        let mostRightPosition: number = this.getMostRightPosition(draggables);

        if (mostRightPosition >= workspaceWidth - workspacePadding) {
            return this.growRight(workspacePadding, mostRightPosition, workspaceWidth, wrapperWidth);
        }

        if (mostLeftPosition > workspacePadding &&
            workspaceWidth > this.initialWorkspaceWidth &&
            mostRightPosition < (workspaceWidth - workspacePadding)
        ) {
            return this.shrinkRight(workspacePadding, mostLeftPosition, mostRightPosition, workspaceWidth);
        }

        return 0;
    }

    private shrinkRight(workspacePadding: number, mostLeftPosition: number, mostRightPosition: number, workspaceWidth: number) {

        const delta =
            Math.min(mostLeftPosition - workspacePadding, (workspaceWidth - mostRightPosition) - workspacePadding);

        this.dropzoneElement.nativeElement.style.width =
            Math.max(this.initialWorkspaceWidth, (workspaceWidth - delta)) + "px";
        return 0;
    }

    private growLeft(workspacePadding: number, mostLeftPosition: number, workspaceWidth: number, wrapperWidth: number) {
        const delta = workspacePadding - mostLeftPosition;
        this.dropzoneElement.nativeElement.style.width =
            Math.max(this.initialWorkspaceWidth, (workspaceWidth + delta)) + "px";
        this.workspaceScrollContainer.nativeElement.scrollLeft += delta;
        this.workspaceScrollContainer.nativeElement.style.width = wrapperWidth + "px";
        return delta;
    }

    private growRight(workspacePadding: number, mostRightPosition: number, workspaceWidth: number, wrapperWidth: number) {
        const delta = workspacePadding - (workspaceWidth - mostRightPosition);
        this.dropzoneElement.nativeElement.style.width =
            Math.max(this.initialWorkspaceWidth, (workspaceWidth + delta)) + "px";
        this.workspaceScrollContainer.nativeElement.style.width = wrapperWidth + "px";
        return 0;
    }

    private getMostRightPosition(draggables: Draggable[]): number {
        let mostRightPosition: number = 0;
        draggables.forEach((draggable) => {

            mostRightPosition =
                Math.max(
                    mostRightPosition,
                    draggable.getDraggableModel().position.x + draggable.getTotalWidth());
        });
        return mostRightPosition;
    }

    private getMostLeftPosition(draggables: Draggable[], workspaceWidth: number) {
        let mostLeftPosition: number = workspaceWidth;
        draggables.forEach(draggable =>
            mostLeftPosition = Math.min(mostLeftPosition, draggable.getDraggableModel().position.x)
        );
        return mostLeftPosition;
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