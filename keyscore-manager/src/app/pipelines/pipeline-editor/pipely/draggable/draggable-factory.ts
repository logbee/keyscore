import {ComponentFactory, ComponentFactoryResolver, Injectable, ViewContainerRef} from "@angular/core";
import {Draggable, Workspace} from "../models/contract";
import {DraggableComponent} from "../draggable.component";
import {DraggableModel} from "../models/draggable.model";

@Injectable()
export class DraggableFactory {

    private componentFactory: ComponentFactory<DraggableComponent>;

    constructor(private resolver: ComponentFactoryResolver) {
        this.componentFactory = this.resolver.resolveComponentFactory(DraggableComponent);
    }

    public createDraggable(container: ViewContainerRef, model: DraggableModel, workspace: Workspace): Draggable {
        const draggableRef = container.createComponent(this.componentFactory);
        draggableRef.instance.draggableModel = model;
        draggableRef.instance.componentRef = draggableRef;
        draggableRef.instance.workspace = workspace;

        return draggableRef.instance;
    }

}