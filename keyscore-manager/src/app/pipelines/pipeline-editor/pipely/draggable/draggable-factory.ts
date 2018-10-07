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
        if(!model.color){
            model = {
                ...model,
                color:this.computeColor(model.blockDescriptor.categories.map(cat => cat.name))
            };
        }
        draggableRef.instance.draggableModel = model;
        draggableRef.instance.componentRef = draggableRef;
        draggableRef.instance.workspace = workspace;

        return draggableRef.instance;
    }

    private computeColor(categories: string[]): string {
        const colors: string[] = ['#cc0000', '#e69138', '#f1c232', '#6aa84f', '#45818e', '#3d85c6', '#674ea7'];
        let categoryHash = this.categoryHashCode(categories.reduce((acc, category) => acc + category, ""));
        return colors[Math.abs(categoryHash % colors.length)];
    }

    private categoryHashCode(cats: string): number {
        let hash = 0, i, chr;
        if (cats.length === 0) return hash;
        for (i = 0; i < cats.length; i++) {
            chr = cats.charCodeAt(i);
            hash = ((hash << 5) - hash) + chr;
            hash |= 0; // Convert to 32bit integer
        }
        return hash;
    }

}