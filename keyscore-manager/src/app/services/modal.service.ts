import {Component, ComponentFactoryResolver, ComponentRef, Injectable, Type, ViewContainerRef} from '@angular/core'

import {AddFilterDialog} from '../streams/add-filter-dialog.component'

/*
 * The mechanism to add/show components dynamically based on:
 * https://medium.com/front-end-hacking/dynamically-add-components-to-the-dom-with-angular-71b0cb535286
 */
@Injectable()
export class ModalService {

    private modalViewContainer: ViewContainerRef;

    private component: ComponentRef<any>;

    constructor(private factoryResolver: ComponentFactoryResolver) { }

    public setRootViewContainerRef(viewContainerRef: ViewContainerRef) {
        this.modalViewContainer = viewContainerRef
    }

    public show(componentType: Type<Component>) {
        const factory = this.factoryResolver.resolveComponentFactory(AddFilterDialog);
        this.component = factory.create(this.modalViewContainer.parentInjector);
        this.modalViewContainer.insert(this.component.hostView);
        this.modalViewContainer.element.nativeElement.className = 'modal fade show';
    }

    public close() {
        if (this.component) {
            this.modalViewContainer.element.nativeElement.className = 'modal hide';
            this.component.destroy();
            this.component = null;
        }
    }
}