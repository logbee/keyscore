import {
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    ComponentRef,
    QueryList,
    ViewChild,
    ViewChildren,
    ViewContainerRef
} from "@angular/core";
import {animate, style, transition, trigger} from "@angular/animations";
import {ParameterComponent} from "../ParameterComponent";
import {Subscription} from "rxjs";

import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {
    ListParameter,
    Parameter,
    ParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter.model";
import {ListParameterDescriptor} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/list-parameter.model";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";


@Component({
    selector: 'parameter-list',
    template: `
        <div class="parameter-list-host">
            <div fxLayout="row" fxLayoutGap="15px" class="parameter-list-header">
                <div fxFlex>
                    <ng-template #addParameterInputContainer>
                    </ng-template>
                </div>
                <button mat-button mat-icon-button (click)="add(addParameterComponentRef.instance.value.value)"
                        fxFlexAlign="center">
                    <mat-icon color="accent">add_circle_outline</mat-icon>
                </button>
            </div>
            <mat-expansion-panel [(expanded)]="panelExpanded">
                <mat-expansion-panel-header [collapsedHeight]="'*'"
                                            [expandedHeight]="'*'" class="sequence-header" fxLayout="row"
                                            fxLayoutAlign="space-between center">
                    <mat-panel-title><span
                        class="ks-expansion-panel-title" translate [translateParams]="{name:descriptor.displayName}">
                        PARAMETER.ADDED_ELEMENTS
                    </span>
                    </mat-panel-title>
                </mat-expansion-panel-header>
                <div cdkDropList (cdkDropListDropped)="drop($event)" class="parameter-list">
                    <div *ngFor="let param of valueParameter;let i=index" cdkDrag class="parameter-list-item"
                         fxLayout="row" fxLayoutAlign="space-around center">
                        <div class="drag-handle" cdkDragHandle>
                            <mat-icon>drag_handle</mat-icon>
                        </div>
                        <div fxFlex>
                            <ng-template #listItemInputContainer>
                            </ng-template>
                        </div>
                        <button mat-button mat-icon-button (click)="remove(i)">
                            <mat-icon color="warn">delete</mat-icon>
                        </button>

                    </div>
                </div>
            </mat-expansion-panel>
            <p class="parameter-list-warn" *ngIf="descriptor.min > 0 && !valueParameter.length" translate
               [translateParams]="{name:descriptor.displayName}">PARAMETER.IS_REQUIRED</p>
            <p class="parameter-list-warn" *ngIf="valueParameter.length < descriptor.min" translate
               [translateParams]="{name:descriptor.displayName,min:descriptor.min}">PARAMETER.LIST_MIN</p>
            <p @max-warn class="parameter-list-warn" *ngIf="maxElementsReached" translate
               [translateParams]="{name:descriptor.displayName,max:descriptor.max}">PARAMETER.LIST_MAX_REACHED</p>
        </div>
    `,
    styleUrls: ['./list-parameter.component.scss', '../../style/parameter-module-style.scss'],
    animations: [
        trigger('max-warn', [
            transition(':leave', [
                style({transform: 'translateY(0)', opacity: 1}),
                animate('0.5s cubic-bezier(.8, 0, 0.5, 1.2)',
                    style({
                        transform: 'translateY(-100%)', opacity: 0
                    }))
            ])
        ])
    ]
})
export class ListParameterComponent extends ParameterComponent<ListParameterDescriptor, ListParameter> implements AfterViewInit {


    @ViewChild('addParameterInputContainer', {
        read: ViewContainerRef,
        static: true
    }) addParameterContainer: ViewContainerRef;
    @ViewChildren('listItemInputContainer', {read: ViewContainerRef}) listItemContainers: QueryList<ViewContainerRef>;

    get values() {
        return this.valueParameter.map(param => param.value);
    }

    addParameterComponentRef: ComponentRef<ParameterComponent<ParameterDescriptor, Parameter>>;
    valueParameter: Parameter[] = [];
    maxElementsReached: boolean = false;
    panelExpanded: boolean = true;

    private _subs$$: Subscription[] = [];
    private _listItemChangeSubs$$: Subscription[] = [];

    constructor(
        private parameterComponentFactory: ParameterComponentFactoryService,
        private parameterFactory: ParameterFactoryService,
        private changeRef: ChangeDetectorRef
    ) {
        super();
    }

    onInit() {
        this.initValueParameters();
    }

    ngAfterViewInit() {
        this._subs$$.push(this.listItemContainers.changes.subscribe(containers => this.updateList(containers)));

        this.createAddParameterComponent();
        this.updateList(this.listItemContainers);
        this.changeRef.detectChanges();
    }

    private updateList(containers: QueryList<ViewContainerRef>) {
        this.unsubscribeListItems();
        containers.forEach((containerRef, index) => {
            this.createListItem(this.valueParameter[index], containerRef, index);
        })
    }

    private createListItem(parameter: Parameter, container: ViewContainerRef, index: number) {
        container.clear();

        const componentRef =
            this.parameterComponentFactory.createParameterComponent(this.descriptor.descriptor.jsonClass, container);
        componentRef.instance.parameter = parameter;
        componentRef.instance.descriptor = this.descriptor.descriptor;
        componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;
        componentRef.instance.showLabel = false;

        this._listItemChangeSubs$$.push(componentRef.instance.emitter.subscribe(parameter =>
            this.itemChanged(parameter, index)));

        componentRef.changeDetectorRef.detectChanges();
    }

    private itemChanged(value: Parameter, index: number) {
        this.valueParameter.splice(index, 1,
            this.parameterFactory.parameterDescriptorToParameter(this.descriptor.descriptor, value.value));
        this.emitChanges();
    }

    remove(index: number) {
        this.valueParameter.splice(index, 1);
        this.emitChanges();
    }

    add(value: any) {
        this.panelExpanded = true;

        if (this.descriptor.max > 0 && this.valueParameter.length >= this.descriptor.max) {
            if (!this.maxElementsReached) {
                this.maxElementsReached = true;
                setTimeout(() => this.maxElementsReached = false, 5000);
            }
            return;
        }
        this.valueParameter.push(this.parameterFactory.parameterDescriptorToParameter(this.descriptor.descriptor, value));
        this.addParameterComponentRef.instance.clear();
        this.emitChanges();

    }

    drop(event: CdkDragDrop<Parameter[]>) {
        moveItemInArray(this.valueParameter, event.previousIndex, event.currentIndex);
        this.emitChanges();
    }

    private emitChanges() {
        const param = this.parameterFactory.parameterDescriptorToParameter(this.descriptor, this.values);
        this.emit(param as ListParameter);
    }

    private createAddParameterComponent() {
        this.addParameterComponentRef = this.parameterComponentFactory.createParameterComponent(
            this.descriptor.descriptor.jsonClass,
            this.addParameterContainer
        );

        this.addParameterComponentRef.instance.parameter =
            this.parameterFactory.parameterDescriptorToParameter(this.descriptor.descriptor);

        this.addParameterComponentRef.instance.descriptor = this.descriptor.descriptor;
        this.addParameterComponentRef.instance.autoCompleteDataList = this.autoCompleteDataList;
        this.addParameterComponentRef.instance.label = this.descriptor.displayName;

        this._subs$$.push(this.addParameterComponentRef.instance.keyUpEnterEvent.subscribe(event => {
            this.add(this.addParameterComponentRef.instance.value.value);
            this.addParameterComponentRef.instance.focus(null);
        }))
    }

    private initValueParameters() {
        this.valueParameter = [];
        ((this.parameter.value) as any[]).forEach(val => {
            this.valueParameter.push(this.parameterFactory.parameterDescriptorToParameter(this.descriptor.descriptor, val))
        })
    }

    private unsubscribeListItems() {
        this._listItemChangeSubs$$.forEach(sub => sub.unsubscribe());
    }

    onDestroy() {
        this.unsubscribeListItems();
        this._subs$$.forEach(sub => sub.unsubscribe());
    }
}
