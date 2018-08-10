import {
    Component,
    ComponentFactoryResolver,
    ComponentRef,
    OnDestroy,
    OnInit,
    ViewChild,
    ViewContainerRef
} from "@angular/core";
import {DropzoneComponent} from "./dropzone.component";
import {v4 as uuid} from "uuid";
import {DraggableComponent} from "./draggable.component";
import {DragService} from "./services/drag.service";

@Component({
    selector: "workspace",
    template: `
        <div class="workspace col-12">
            <div class="row">
                <ng-template #dropzoneContainer></ng-template>
            </div>
        </div>
    `
})

export class WorkspaceComponent implements OnInit, OnDestroy {
    @ViewChild("dropzoneContainer", {read: ViewContainerRef}) dropzoneContainer: ViewContainerRef;

    public id: string;

    public dropzones: ComponentRef<DropzoneComponent>[] = [];
    public draggables: ComponentRef<DraggableComponent>[] = [];

    constructor(private resolver: ComponentFactoryResolver, private dragService: DragService) {
        this.id = uuid();

        dragService.dragStart$.subscribe(dragStartEvent => {

        })

    }

    private createDropzoneComponent() {
        const dropzoneFactory = this.resolver.resolveComponentFactory(DropzoneComponent);
        const dropzoneRef = this.dropzoneContainer.createComponent(dropzoneFactory);
        this.dropzones.push(dropzoneRef);
        dropzoneRef.instance.workspace = this;
        dropzoneRef.instance.dropzoneModel = {dropzoneType: "general", dropzoneRadius: 0};

    }

    private createDraggableComponent(dropzone: ComponentRef<DropzoneComponent>) {
        const draggableFactory = this.resolver.resolveComponentFactory(DraggableComponent);
        const draggableRef = dropzone.instance.draggableContainer.createComponent(draggableFactory);
        this.draggables.push(draggableRef);
        draggableRef.instance.workspace = this;
        draggableRef.instance.draggableModel = {
            name: "Test" + Math.random(),
            hasAbsolutePosition: false,
            dropzoneType: "general"
        };

    }

    public ngOnInit() {
        for (let i = 0; i < 2; i++) {
            this.createDropzoneComponent();
        }
        this.dropzones.forEach(dropzone => {
            for (let i = 0; i < 2; i++) {
                this.createDraggableComponent(dropzone);
            }
        })
    }

    public ngOnDestroy() {

    }

}
