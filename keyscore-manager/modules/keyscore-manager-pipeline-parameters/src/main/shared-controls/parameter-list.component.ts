import {
    AfterContentInit,
    Component,
    ContentChild, ContentChildren, ElementRef,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
    TemplateRef,
    ViewChild, ViewChildren
} from "@angular/core";
import {animate, style, transition, trigger} from "@angular/animations";
import {ParameterComponent} from "../parameters/ParameterComponent";
import {Parameter} from "../parameters/parameter.model";
import {Subscription} from "rxjs";
import {ListParameter, ListParameterDescriptor} from "../parameters/text-list-parameter/text-list-parameter.model";
import {CdkDragDrop} from "@angular/cdk/drag-drop";
import {ParameterListItemDirective} from "./parameter-list-item.directive";


@Component({
    selector: 'parameter-list',
    template: `
        <div class="parameter-list-host">
            <div fxLayout="row-reverse" class="parameter-list-header">
                <button mat-button mat-icon-button (click)="add()" fxFlexAlign="center">
                    <mat-icon color="accent">add_circle_outline</mat-icon>
                </button>
                <ng-content
                        *ngTemplateOutlet="parameterComponentTemplateRef;context:{
                                descriptor:descriptor,
                                parameter:addParameter,
                                add:addEventNgTemplate
                            }">
                </ng-content>
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
                    <div *ngFor="let param of valueParameter;let i=index" cdkDrag class="parameter-list-item"
                         fxLayout="row-reverse">
                        <button mat-button mat-icon-button (click)="remove(i)" fxFlexAlign="center">
                            <mat-icon color="warn">delete</mat-icon>
                        </button>
                        <ng-content
                                *ngTemplateOutlet="parameterListComponentsTemplateRef;context:{
                                    descriptor:descriptor.descriptor,
                                    parameter:param,
                                    index:i,
                                    change:changeEventNgTemplate
                                }">

                        </ng-content>
                        <div class="drag-handle" cdkDragHandle fxFlexAlign="center">
                            <mat-icon>drag_handle</mat-icon>
                        </div>
                    </div>
                </div>
            </mat-expansion-panel>
            <p class="parameter-list-warn" *ngIf="descriptor.mandatory && !valueParameter.length">
                {{descriptor.displayName}}
                is
                required!</p>
            <p class="parameter-list-warn" *ngIf="valueParameter.length < descriptor.min">{{descriptor.displayName}}
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
export class ParameterListComponent implements OnInit, OnDestroy, AfterContentInit {
    @Input() parameterComponentTemplateRef: TemplateRef<ParameterComponent<ListParameterDescriptor, ListParameter>>;
    @Input() parameterListComponentsTemplateRef: TemplateRef<ParameterComponent<ListParameterDescriptor, ListParameter>>;

    @Input() descriptor: ListParameterDescriptor;
    @Input() parameter: ListParameter;
    @Input() addParameter: Parameter;

    @Input() valueParameter: Parameter[] = [];

    @Output() onAdd: EventEmitter<any> = new EventEmitter<any>();
    @Output() onRemove: EventEmitter<number> = new EventEmitter<number>();
    @Output() onElementChanged: EventEmitter<{ index: number, value: any }> = new EventEmitter();
    @Output() onReorder: EventEmitter<CdkDragDrop<Parameter[]>> = new EventEmitter<CdkDragDrop<Parameter[]>>();

    private addEventNgTemplate: EventEmitter<any> = new EventEmitter();
    private changeEventNgTemplate: EventEmitter<[number, any]> = new EventEmitter<[number, any]>();

    private maxElementsReached: boolean = false;

    private subs: Subscription[] = [];

    private static refGenerator: number = 0;

    get value() {
        return this.valueParameter.map(param => param.value);
    }

    ngOnInit() {
        this.subs.push(this.addEventNgTemplate.subscribe(val => this.add(val)));
        this.subs.push(this.changeEventNgTemplate.subscribe(val => this.onChange(val[1], val[0])));

    }

    ngAfterContentInit() {
        console.log("AddParameterInput", this.parameterComponentTemplateRef);
    }

    onChange(value: any, index: number) {
        if (index >= 0) {
            this.onElementChanged.emit({index: index, value: value});
        }
    }

    remove(index: number) {
        this.onRemove.emit(index);
    }

    add(value: any) {
        console.log("Value Params", this.valueParameter);
        if (this.valueParameter.length === this.descriptor.max) {
            if(!this.maxElementsReached) {
                this.maxElementsReached = true;
                setTimeout(() => this.maxElementsReached = false, 5000);
            }
            return;
        }
        //this.newInputRef.clear();
        this.onAdd.emit(value);
    }

    private drop(event: CdkDragDrop<Parameter[]>) {
        this.onReorder.emit(event);
    }

    ngOnDestroy() {
        this.subs.forEach(sub => sub.unsubscribe());
    }

    private generateId(): string {
        return `list-${ParameterListComponent.refGenerator++}`;
    }

}