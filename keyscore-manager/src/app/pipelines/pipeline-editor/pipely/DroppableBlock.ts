import {Draggable} from "@shopify/draggable";
import {closest} from "@shopify/draggable";

export interface DroppableBlockOptions {
    draggable: string;
    dropzone: string;
}

const onDragStart = Symbol("onDragStart");
const onDragMove = Symbol("onDragMove");
const onDragStop = Symbol("onDragStop");
const onDrop = Symbol("onDrop");
const closestDropzone = Symbol("closestDropzone");
const getDropzones = Symbol("getDropzones");

export default class DroppableBlock extends Draggable {
    private dropzones: any;
    private initialDropzone: any;
    private lastDropzone: any;

    // private onDragMove: any = null;

    constructor(public containers: NodeListOf<Element>, public options: DroppableBlockOptions) {
        super(containers, options);

        this[onDragMove] = this[onDragMove].bind(this);
        this[onDragStart] = this[onDragStart].bind(this);
        this[onDragStop] = this[onDragStop].bind(this);

        super.on("drag:move", this[onDragMove])
            .on("drag:start", this[onDragStart])
            .on("drag:stop", this[onDragStop]);

    }

    public [onDragStart](event) {
        this.dropzones = [...this[getDropzones]()];
        console.log("Dropzones: ", this.dropzones);
        const dropzone = closest(event.sensorEvent.target, this.options.dropzone);

        if (!dropzone) {
            event.cancel();
            return;
        }

        /*const droppableStartEvent = new DroppableStartEvent({
            dragEvent: event,
            dropzone,
        });

        this.trigger(droppableStartEvent);

        if (droppableStartEvent.canceled()) {
            event.cancel();
            return;
        }
*/
        this.initialDropzone = dropzone;

        for (const dropzoneElement of this.dropzones) {
            if (dropzoneElement.classList.contains("draggable-dropzone--occupied")) {
                continue;
            }

            dropzoneElement.classList.add("draggable-dropzone--active");
        }

    }

    public [onDragMove](event) {

        //console.log("new MOVE", event);
    }

    public [onDragStop](event) {

        const dropzone = this[closestDropzone](event.sensorEvent.target);
        const overEmptyDropzone = dropzone && !dropzone.classList.contains("draggable-dropzone--occupied");

        if (overEmptyDropzone) {
            this[onDrop](event, dropzone);
        }

        for (const currentDropzone of this.dropzones) {
            currentDropzone.classList.remove("draggable-dropzone--active");
        }

        if (this.lastDropzone && this.lastDropzone !== this.initialDropzone) {
            this.initialDropzone.classList.remove("draggable-dropzone--active");
        }

        this.dropzones = null;
        this.lastDropzone = null;
        this.initialDropzone = null;
    }

    public [onDrop](event, dropzone) {
        dropzone.appendChild(event.source);
        dropzone.classList.add("draggable-dropzone--occupied");
        event.source.style.position = "absolute";
        event.source.style.top = (event.source.parentNode.getBoundingClientRect().top + 100) + "px";
    }

    private [closestDropzone](target) {
        if (!this.dropzones) {
            return null;
        }

        return closest(target, this.dropzones);
    }

    private [getDropzones]() {
        const dropzone: any = this.options.dropzone;

        if (typeof dropzone === "string") {
            return document.querySelectorAll(dropzone);
        } else if (dropzone instanceof NodeList || dropzone instanceof Array) {
            return dropzone;
        } else if (typeof dropzone === "function") {
            return dropzone();
        } else {
            return [];
        }
    }

    // public onDragMove(event: any): void {
    //
    // }
}
