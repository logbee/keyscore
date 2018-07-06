import {ComponentFactoryResolver, ComponentRef, Injectable, ViewContainerRef} from "@angular/core";
import "jquery";

/*
 * The mechanism to add/show components dynamically based on:
 * https://medium.com/front-end-hacking/dynamically-add-components-to-the-dom-with-angular-71b0cb535286
 */
@Injectable()
export class ModalService {

    private modalViewContainer: ViewContainerRef;

    private component: ComponentRef<any>;

    constructor(private factoryResolver: ComponentFactoryResolver) {
    }

    public setRootViewContainerRef(viewContainerRef: ViewContainerRef) {
        this.modalViewContainer = viewContainerRef;
    }

    public show(componentType: any) {
        const o = this;

        const factory = this.factoryResolver.resolveComponentFactory(componentType);
        this.component = factory.create(this.modalViewContainer.parentInjector);
        this.modalViewContainer.insert(this.component.hostView);

        // it's ugly but works...
        jQuery(".modal").modal("show");
        jQuery(".modal").on("hide.bs.modal", (e) => {
            if (o.component) {
                o.component.destroy();
                o.component = null;
            }
        });
    }

    public close() {
        jQuery(".modal").modal("hide");
    }
}
