import {
    AfterViewInit,
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
import {Parameter, ParameterDescriptor} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter.model";
import {
    ListParameterDescriptor,
    ListParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-lists/list-parameter.model";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";


@Component({
    selector: 'parameter-list',
    template: `
        <div class="parameter-list-host">
            <div fxLayout="row" fxLayoutGap="15px" class="parameter-list-header">
                <div fxFlex="90">
                    <ng-template #addParameterInputContainer>
                    </ng-template>
                </div>
                <button mat-button mat-icon-button (click)="add(_addParameterComponentRef.instance.value.value)"
                        fxFlexAlign="center" fxFlex="10">
                    <mat-icon color="accent">add_circle_outline</mat-icon>
                </button>
            </div>
            <mat-expansion-panel [(expanded)]="_panelExpanded">
                <mat-expansion-panel-header [collapsedHeight]="'*'"
                                            [expandedHeight]="'*'" class="sequence-header" fxLayout="row"
                                            fxLayoutAlign="space-between center">
                    <mat-panel-title><span
                            class="ks-expansion-panel-title">Added Elements for {{descriptor.displayName}}</span>
                    </mat-panel-title>
                </mat-expansion-panel-header>
                <div cdkDropList (cdkDropListDropped)="drop($event)" class="parameter-list">
                    <div *ngFor="let param of _valueParameter;let i=index" cdkDrag class="parameter-list-item"
                         fxLayout="row-reverse">
                        <button mat-button mat-icon-button (click)="remove(i)" fxFlexAlign="center">
                            <mat-icon color="warn">delete</mat-icon>
                        </button>
                        <ng-template #listItemInputContainer>
                        </ng-template>
                        <div class="drag-handle" cdkDragHandle fxFlexAlign="center">
                            <mat-icon>drag_handle</mat-icon>
                        </div>
                    </div>
                </div>
            </mat-expansion-panel>
            <p class="parameter-list-warn" *ngIf="descriptor.mandatory && !_valueParameter.length">
                {{descriptor.displayName}}
                is
                required!</p>
            <p class="parameter-list-warn" *ngIf="_valueParameter.length < descriptor.min">{{descriptor.displayName}}
                needs at least {{descriptor.min}} {{descriptor.min > 1 ? 'elements' : 'element'}}.</p>
            <p @max-warn class="parameter-list-warn" *ngIf="_maxElementsReached">
                You reached the maximum number of elements. {{descriptor.displayName}}
                allows a maximum of {{descriptor.max}} elements.</p>
        </div>
    `,
    styleUrls: ['./list-parameter.component.scss', '../../style/parameter-module-style.scss'],
    animations: [
        trigger('max-warn', [
            transition(':enter', [
                style({transform: 'scale(0.5)', opacity: 0}),
                animate('1s cubic-bezier(.8, -0.6, 0.2, 1.2)',
                    style({transform: 'scale(1)', opacity: 1}))
            ]),
            transition(':leave', [
                style({transform: 'scale(1)', opacity: 1, height: '*'}),
                animate('1s cubic-bezier(.8, 0, 0.5, 1.2)',
                    style({
                        transform: 'scale(0.5)', opacity: 0,
                        height: '0px', margin: '0px'
                    }))
            ])
        ])
    ]
})
export class ListParameterComponent extends ParameterComponent<ListParameterDescriptor, ListParameter> implements AfterViewInit {


    @ViewChild('addParameterInputContainer', {read: ViewContainerRef}) addParameterContainer: ViewContainerRef;
    @ViewChildren('listItemInputContainer', {read: ViewContainerRef}) listItemContainers: QueryList<ViewContainerRef>;

    get values() {
        return this._valueParameter.map(param => param.value);
    }

    private _addParameterComponentRef: ComponentRef<ParameterComponent<ParameterDescriptor, Parameter>>;

    private _valueParameter: Parameter[] = [];

    private _maxElementsReached: boolean = false;

    private _subs$$: Subscription[] = [];
    private _listItemChangeSubs$$: Subscription[] = [];

    private _panelExpanded: boolean = true;

    constructor(
        private parameterComponentFactory: ParameterComponentFactoryService,
        private parameterFactory: ParameterFactoryService
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
    }

    itemChanged(value: Parameter, index: number) {
        this._valueParameter.splice(index, 1,
            this.parameterFactory.parameterDescriptorToParameter(this.descriptor.descriptor, value.value));
        this.emitChanges();
    }

    remove(index: number) {
        this._valueParameter.splice(index, 1);
        this.emitChanges();
    }

    add(value: any) {
        this._panelExpanded = true;

        if (this.descriptor.max > 0 && this._valueParameter.length >= this.descriptor.max) {
            if (!this._maxElementsReached) {
                this._maxElementsReached = true;
                setTimeout(() => this._maxElementsReached = false, 5000);
            }
            return;
        }
        this._valueParameter.push(this.parameterFactory.parameterDescriptorToParameter(this.descriptor.descriptor, value));
        this._addParameterComponentRef.instance.clear();
        this.emitChanges();

    }

    private drop(event: CdkDragDrop<Parameter[]>) {
        moveItemInArray(this._valueParameter, event.previousIndex, event.currentIndex);
        this.emitChanges();
    }


    private createAddParameterComponent() {
        this._addParameterComponentRef = this.parameterComponentFactory.createParameterComponent(
            this.descriptor.descriptor.jsonClass,
            this.addParameterContainer
        );

        this._addParameterComponentRef.instance.parameter =
            this.parameterFactory.parameterDescriptorToParameter(this.descriptor.descriptor);

        this._addParameterComponentRef.instance.descriptor = this.descriptor.descriptor;
        this._addParameterComponentRef.instance.autoCompleteDataList = this.autoCompleteDataList;
        this._addParameterComponentRef.instance.label = this.descriptor.displayName;

        this._subs$$.push(this._addParameterComponentRef.instance.keyUpEnterEvent.subscribe(event =>
            this.add(this._addParameterComponentRef.instance.value.value)));
    }

    private updateList(containers: QueryList<ViewContainerRef>) {
        this.unsubscribeListItems();
        containers.forEach((containerRef, index) => {
            this.createListItem(this._valueParameter[index], containerRef, index);
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

    private initValueParameters() {
        this._valueParameter = [];
        this.parameter.value.forEach(val => {
            this._valueParameter.push(this.parameterFactory.parameterDescriptorToParameter(this.descriptor.descriptor, val))
        })
    }

    private emitChanges() {
        this.emit(this.parameterFactory.parameterDescriptorToParameter(this.descriptor, this.values));
    }

    private unsubscribeListItems() {
        this._listItemChangeSubs$$.forEach(sub => sub.unsubscribe());
    }

    onDestroy() {
        this.unsubscribeListItems();
        this._subs$$.forEach(sub => sub.unsubscribe());
    }
}