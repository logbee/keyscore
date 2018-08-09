import {
    Component, ComponentFactoryResolver, ComponentRef, ElementRef, OnDestroy, OnInit, ViewChild,
    ViewContainerRef
} from "@angular/core";
import {Workspace} from "./workspace";
import {DropzoneComponent} from "./dropzone.component";
import {v4 as uuid} from "uuid";
import {DraggableComponent} from "./draggable.component";

@Component({
    selector: "workspace",
    template: `
        <div class="workspace col-12">
            <div #dropzoneContainer class="row">
                
            </div>
        </div>
    `
})

export class WorkspaceComponent implements OnInit, OnDestroy {
    @ViewChild("dropzoneContainer", {read: ViewContainerRef}) dropzoneContainer: ViewContainerRef;
    public dropzones: ComponentRef<DropzoneComponent>[] = [];

    constructor(private resolver: ComponentFactoryResolver) {

    }

    private createDropzoneComponent() {
        const dropzoneFactory = this.resolver.resolveComponentFactory(DropzoneComponent);
        const componentRef = this.dropzoneContainer.createComponent(dropzoneFactory);
        this.dropzones.push(componentRef);
        componentRef.instance.id = uuid();
        componentRef.instance.workspace = this;


    }

    public ngOnInit() {
        for(let i=0;i<2;i++){
            this.createDropzoneComponent();
        }
    }

    public ngOnDestroy() {

    }

}
