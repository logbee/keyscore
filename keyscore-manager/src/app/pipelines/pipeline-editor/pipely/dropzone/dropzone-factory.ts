import {ComponentFactory, ComponentFactoryResolver, Injectable, ViewContainerRef} from "@angular/core";
import {DropzoneComponent} from "../dropzone.component";
import {Draggable, Dropzone} from "../models/contract";
import {DropzoneType} from "../models/dropzone-type";
import {ToolbarDropzoneLogic} from "./toolbar-dropzone-logic";
import {WorkspaceDropzoneLogic} from "./workspace-dropzone-logic";
import {ConnectorDropzoneLogic} from "./connector-dropzone-logic";
import {ToolbarDropzoneSubcomponent} from "./toolbar-dropzone-subcomponent";
import {WorkspaceDropzoneSubcomponent} from "./workspace-dropzone-subcomponent";
import {ConnectorDropzoneSubcomponent} from "./connector-dropzone-subcomponent";

@Injectable()
export class DropzoneFactory {

    private componentFactory: ComponentFactory<DropzoneComponent>;

    constructor(private resolver: ComponentFactoryResolver) {
        this.componentFactory = this.resolver.resolveComponentFactory(DropzoneComponent);

    }

    public createToolbarDropzone(container: ViewContainerRef): Dropzone {
        const dropzoneRef = container.createComponent(this.componentFactory);
        dropzoneRef.instance.dropzoneModel = {
            dropzoneRadius: 0,
            dropzoneType: DropzoneType.Toolbar,
            acceptedDraggableTypes: [],
            owner: null
        };
        dropzoneRef.instance.logic = new ToolbarDropzoneLogic();

        const toolbarSubFactory = this.resolver.resolveComponentFactory(ToolbarDropzoneSubcomponent);
        const subRef = dropzoneRef.instance.dropzoneContainer.createComponent(toolbarSubFactory);
        dropzoneRef.instance.subComponent = subRef.instance;
        return dropzoneRef.instance;
    }

    public createWorkspaceDropzone(container: ViewContainerRef): Dropzone {
        const dropzoneRef = container.createComponent(this.componentFactory);
        dropzoneRef.instance.dropzoneModel = {
            dropzoneRadius: 0,
            dropzoneType: DropzoneType.Workspace,
            acceptedDraggableTypes: ["general"],
            owner: null
        };
        dropzoneRef.instance.logic = new WorkspaceDropzoneLogic(dropzoneRef.instance);

        const workspaceSubFactory = this.resolver.resolveComponentFactory(WorkspaceDropzoneSubcomponent);
        const subRef = dropzoneRef.instance.dropzoneContainer.createComponent(workspaceSubFactory);
        dropzoneRef.instance.subComponent = subRef.instance;
        return dropzoneRef.instance;
    }

    public createConnectorDropzone(container: ViewContainerRef,
                                   owner: Draggable,
                                   acceptedDraggables: string[]): Dropzone {
        const dropzoneRef = container.createComponent(this.componentFactory);
        dropzoneRef.instance.dropzoneModel = {
            dropzoneRadius: 40,
            dropzoneType: DropzoneType.Connector,
            acceptedDraggableTypes: acceptedDraggables,
            owner: owner
        };
        dropzoneRef.instance.logic = new ConnectorDropzoneLogic(dropzoneRef.instance);

        const connectorSubFactory = this.resolver.resolveComponentFactory(ConnectorDropzoneSubcomponent);

        const subRef = dropzoneRef.instance.dropzoneContainer.createComponent(connectorSubFactory);
        dropzoneRef.instance.subComponent = subRef.instance;
        return dropzoneRef.instance;
    }
}