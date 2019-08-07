import {
    AfterContentInit, AfterViewInit,
    Component, ComponentRef,
    ContentChild, ContentChildren, ElementRef,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output, QueryList,
    TemplateRef,
    ViewChild, ViewChildren, ViewContainerRef
} from "@angular/core";
import {animate, style, transition, trigger} from "@angular/animations";
import {ParameterComponent} from "../parameters/ParameterComponent";
import {Parameter, ParameterDescriptor} from "../parameters/parameter.model";
import {Subscription} from "rxjs";
import {ListParameter, ListParameterDescriptor} from "../parameters/text-list-parameter/text-list-parameter.model";
import {CdkDragDrop} from "@angular/cdk/drag-drop";
import {ParameterListItemDirective} from "./parameter-list-item.directive";
import {ParameterComponentFactoryService} from "../service/parameter-component-factory.service";
import {ParameterFactoryService} from "../service/parameter-factory.service";


@Component({
    selector: 'parameter-list',
    template: `
        <div class="parameter-list-host">
            <div fxLayout="row-reverse" class="parameter-list-header">
                <button mat-button mat-icon-button (click)="add(_addParameterComponentRef.instance.value.value)" fxFlexAlign="center">
                    <mat-icon color="accent">add_circle_outline</mat-icon>
                </button>
                <ng-template #addParameterInputContainer>
                </ng-template>
            </div>
            <mat-expansion-panel [expanded]="true">
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
            <p @max-warn class="parameter-list-warn" *ngIf="maxElementsReached">
                You reached the maximum number of elements. {{descriptor.displayName}}
                allows a maximum of {{descriptor.max}} elements.</p>
        </div>
    `,
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
export class ParameterListComponent extends ParameterComponent<ListParameterDescriptor, ListParameter> implements AfterViewInit {

    @ViewChild('addParameterInputContainer', {read: ViewContainerRef}) addParameterContainer: ViewContainerRef;
    @ViewChildren('listItemInputContainer', {read: ViewContainerRef}) listItemContainers: QueryList<ViewContainerRef>;

    private _addParameterComponentRef: ComponentRef<ParameterComponent<ParameterDescriptor, Parameter>>;

    private _valueParameter: Parameter[] = [];

    private maxElementsReached: boolean = false;

    private subs: Subscription[] = [];

    get values() {
        return this._valueParameter.map(param => param.value);
    }

    constructor(
        private parameterComponentFactory: ParameterComponentFactoryService,
        private parameterFactory: ParameterFactoryService
    ) {
        super();
    }

    onInit() {
        this.buildValueParameters();

    }

    ngAfterViewInit(){
        this.createAddParameterComponent();
        this.listItemContainers.forEach((containerRef,index) => {
            this.createListItem(this._valueParameter[index],containerRef);
        })

    }

    onChange(value: any, index: number) {

    }

    remove(index: number) {
        this._valueParameter.splice(index,1);
    }

    add(value: any) {
        console.log("Value Params", this._valueParameter);
        if (this._valueParameter.length === this.descriptor.max) {
            if (!this.maxElementsReached) {
                this.maxElementsReached = true;
                setTimeout(() => this.maxElementsReached = false, 5000);
            }
            return;
        }
        this._valueParameter.push(this.parameterFactory.newParameterDescriptorToParameter(this.descriptor.descriptor,value))
        //this.newInputRef.clear();

    }

    private drop(event: CdkDragDrop<Parameter[]>) {

    }

    onDestroy() {
        this.subs.forEach(sub => sub.unsubscribe());
    }

    private createAddParameterComponent() {
        this._addParameterComponentRef = this.parameterComponentFactory.createParameterComponent(this.descriptor.descriptor.jsonClass, this.addParameterContainer);
        this._addParameterComponentRef.instance.parameter = this.parameterFactory.newParameterDescriptorToParameter(this.descriptor.descriptor);
        this._addParameterComponentRef.instance.descriptor = this.descriptor.descriptor;
        this._addParameterComponentRef.instance.autoCompleteDataList = this.autoCompleteDataList;
    }

    private createListItem(parameter: Parameter,container:ViewContainerRef) {
        const componentRef = this.parameterComponentFactory.createParameterComponent(this.descriptor.descriptor.jsonClass, container);
        componentRef.instance.parameter = parameter;
        componentRef.instance.descriptor = this.descriptor.descriptor;
        componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;
    }

    private buildValueParameters() {
        this._valueParameter = [];
        this.parameter.value.forEach(val => {
            this._valueParameter.push(this.parameterFactory.newParameterDescriptorToParameter(this.descriptor.descriptor, val))
        })
    }

}