import {Component, OnDestroy, OnInit} from "@angular/core";
import {Subject} from "rxjs/index";
import {Sortable} from "@shopify/draggable";
import {Plugins} from "@shopify/draggable";
import {Draggable} from "@shopify/draggable";
import {Droppable} from "@shopify/draggable";

import "./style/pipely-style.css";
import DroppableBlock from "./DroppableBlock";

@Component({
    selector: "pipely-workspace",
    template:
            `
        <div class="pipely-wrapper">
            <div class="row">
                <h1>Pipely</h1>
            </div>
            <div class="row wrapper">
                <div class="col-6 workspace--isDropzone">
                    <div class="Block Block--isDraggable">First Element<span
                            class="workspace--isDropzone">DropZone</span></div>
                    <div class="Block Block--isDraggable">Second Element<span
                            class="workspace--isDropzone">DropZone</span></div>
                    <div class="Block Block--isDraggable">Third Element<span
                            class="workspace--isDropzone">DropZone</span></div>
                </div>
                <div class="col-6 " id="workspace">
                    <div class="workspace--isDropzone innerWorkspace"></div>
                    <div class="workspace--isDropzone innerWorkspace absoluteWorkspace"></div>

                </div>
            </div>
        </div>

    `
})

export class PipelyComponent implements OnInit, OnDestroy {
    private alive$: Subject<void> = new Subject();
    private droppable;

    public ngOnDestroy() {
        this.alive$.next();
    }

    public ngOnInit() {
        const containers = document.querySelectorAll(".wrapper");
        let mouseX: number;
        let mouseY: number;

        this.droppable = new DroppableBlock(containers, {
            draggable: ".Block--isDraggable",
            dropzone: ".workspace--isDropzone"
        });

        this.droppable.on("drag:start", (evt) => {
            const originalSource = evt.data.originalSource;
            const source = evt.data.source;
            const parent = evt.data.source.parentNode;
            // source.style.position = "relative";
        });

        /*this.droppable.on("droppable:dropped", (evt) => {
            console.log(evt);

            mouseX = evt.data.dragEvent.data.sensorEvent.clientX;
            mouseY = evt.data.dragEvent.data.sensorEvent.clientY;

        });*/

        this.droppable.on("drag:stop", (evt) => {
            console.log(evt);

            const originalSource = evt.data.originalSource;
            const source = evt.data.source;
            source.parentNode.classList.remove("draggable-dropzone--occupied");
            /*if (source.parentNode.classList.contains("absoluteWorkspace")) {
                const parentPositionRect = source.parentNode.getBoundingClientRect();
                originalSource.style.position = "absolute";
                originalSource.style.left = (mouseX - parentPositionRect.left) + "px";
                originalSource.style.top = (mouseY - parentPositionRect.top) + "px";
                console.log("x:" + mouseX + " y:" + mouseY);

            }*/

        });

    }

}
