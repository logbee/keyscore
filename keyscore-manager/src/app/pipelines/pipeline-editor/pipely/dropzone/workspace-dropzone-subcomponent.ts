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

    resizeWorkspace(draggables: Draggable[], workspacePadding: number): number {
        const workspaceWidth = this.dropzoneElement.nativeElement.scrollWidth;
        console.log("WorkspaceWidth: " + workspaceWidth);
        const wrapperWidth = this.workspaceScrollContainer.nativeElement.offsetWidth;
        const draggableModels = draggables
            .map(draggable => draggable.getDraggableModel());

        let mostLeftPosition: number = workspaceWidth;
        draggableModels.forEach(draggableModel =>
            mostLeftPosition = Math.min(mostLeftPosition, draggableModel.position.x)
        );

        if (mostLeftPosition <= workspacePadding) {
            return this.growLeft(workspacePadding, mostLeftPosition, workspaceWidth, wrapperWidth);
        }

        let mostRightPosition: number = 0;
        draggables.forEach((draggable, index, array) => {
            console.log("X: "+draggable.getAbsoluteDraggablePosition().x);
            console.log("Width: "+draggable.getDraggableSize().width);
            mostRightPosition = Math.max(mostRightPosition, draggable.getAbsoluteDraggablePosition().x + draggable.getDraggableSize().width);
            console.log("Right: " + mostRightPosition);
        });
        console.log("Initial Workspace Width: " + this.initialWorkspaceWidth);
        if (mostLeftPosition > workspacePadding &&
            workspaceWidth > this.initialWorkspaceWidth) {
            return this.shrinkRight(workspacePadding, mostLeftPosition, mostRightPosition, workspaceWidth, wrapperWidth);
        }

        /*if (mostRightPosition >= workspaceWidth - workspacePadding) {
            const delta = workspacePadding - (workspaceWidth - mostRightPosition);
            this.dropzoneElement.nativeElement.style.width =
                Math.max(this.initialWorkspaceWidth, (workspaceWidth + delta)) + "px";
            //this.workspaceScrollContainer.nativeElement.scrollLeft += delta;
            this.workspaceScrollContainer.nativeElement.style.width = wrapperWidth + "px";
            return 0;
        }*/

        return 0;
    }

    private shrinkRight(workspacePadding: number, mostLeftPosition: number, mostRightPosition: number, workspaceWidth: number, wrapperWidth: number) {

        console.log("Shrink");
        const delta =
            Math.min(mostLeftPosition - workspacePadding, (workspaceWidth - mostRightPosition) - workspacePadding);
        console.log("Delta: " + delta);
        console.log("MostLeft: " + mostLeftPosition);
        console.log("workspacePadding: " + workspacePadding);
        console.log("mostRight: " + mostRightPosition);
        console.log("workspaceWidth: " + workspaceWidth);
        this.dropzoneElement.nativeElement.style.width =
            Math.max(this.initialWorkspaceWidth, (workspaceWidth - delta)) + "px";
        return 0;
    }

    private growLeft(workspacePadding: number, mostLeftPosition: number, workspaceWidth: number, wrapperWidth: number) {
        console.log("Grow Left");
        const delta = workspacePadding - mostLeftPosition;
        this.dropzoneElement.nativeElement.style.width =
            Math.max(this.initialWorkspaceWidth, (workspaceWidth + delta)) + "px";
        this.workspaceScrollContainer.nativeElement.scrollLeft += delta;
        this.workspaceScrollContainer.nativeElement.style.width = wrapperWidth + "px";
        return delta;
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