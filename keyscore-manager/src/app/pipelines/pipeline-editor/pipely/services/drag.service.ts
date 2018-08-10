import {Injectable} from "@angular/core";
import {Subject} from "rxjs/index";
import {DragMoveEvent} from "../events/drag-move.event";
import {DragStartEvent} from "../events/drag-start.event";
import {DragStopEvent} from "../events/drag-stop.event";

@Injectable()
export class DragService {

    private dragStartSource = new Subject<DragStartEvent>();
    private dragMoveSource = new Subject<DragMoveEvent>();
    private dragStopSource = new Subject<DragStopEvent>();

    dragStart$ = this.dragStartSource.asObservable();
    dragMove$ = this.dragMoveSource.asObservable();
    drag

    triggerDragStart(event: DragStartEvent) {
        this.dragStartSource.next(event);
    }

    triggerDragMove(event: DragMoveEvent) {
        console.log("DragMoveEvent: ", event);
        this.dragMoveSource.next(event);
    }
}