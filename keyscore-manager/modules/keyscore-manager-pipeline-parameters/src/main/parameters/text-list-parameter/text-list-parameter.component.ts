import {Component, HostBinding, ViewChild} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {TextListParameter, TextListParameterDescriptor} from "./text-list-parameter.model";
import {TextParameter} from "../text-parameter/text-parameter.model";
import {animate, keyframes, style, transition, trigger} from "@angular/animations";
import {TextParameterComponent} from "../text-parameter/text-parameter.component";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";

@Component({
    selector: 'parameter-text-list',
    template: `
        <div class="parameter-list-host">
        <div fxLayout="row-reverse" class="parameter-list-header">
            <button mat-button mat-icon-button (click)="add(newInput.value.value)" fxFlexAlign="center">
                <mat-icon color="accent">add_circle_outline</mat-icon>
            </button>
            <parameter-text #newInput fxFlex
                            [descriptor]="descriptor"
                            [parameter]="_newParameter"
                            (keyUpEnter)="add(newInput.value.value)">
            </parameter-text>
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
                <div *ngFor="let param of _valueParams;let i=index" cdkDrag class="parameter-list-item"
                     fxLayout="row-reverse">
                    <button mat-button mat-icon-button (click)="remove(i)" fxFlexAlign="center">
                        <mat-icon color="warn">delete</mat-icon>
                    </button>
                    <parameter-text fxFlex [showLabel]="false"
                                    [descriptor]="descriptor.descriptor"
                                    [parameter]="param" (parameter)="onChange($event,i)">
                    </parameter-text>
                    <div class="drag-handle" cdkDragHandle fxFlexAlign="center">
                        <mat-icon>drag_handle</mat-icon>
                    </div>
                </div>
            </div>
        </mat-expansion-panel>
        <p class="parameter-list-warn" *ngIf="descriptor.mandatory && !_valueParams.length">{{descriptor.displayName}}
            is
            required!</p>
        <p class="parameter-list-warn" *ngIf="_valueParams.length < descriptor.min">{{descriptor.displayName}}
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
export class TextListParameterComponent extends ParameterComponent<TextListParameterDescriptor, TextListParameter> {

    private static refGenerator = 0;

    @ViewChild('newInput') newInputRef: TextParameterComponent;

    private _newParameter = new TextParameter({id: "new"}, "");
    private _valueParams: TextParameter[] = [];

    private maxElementsReached: boolean = false;

    get value() {
        return this._valueParams.map(param => param.value);
    }

    onInit() {
        this.parameter.value.forEach(val => {
            this._valueParams.push(new TextParameter({id: this.generateId()}, val))
        });
        console.log("mock Params: ", this._valueParams);
    }

    onChange(textParameter: TextParameter, index: number) {
        if (index >= 0) {
            this._valueParams.splice(index, 1, new TextParameter({id: textParameter.ref.id}, textParameter.value));
            this.emitter.emit(new TextListParameter(this.descriptor.ref, this.value));
        }
    }

    remove(index: number) {
        this._valueParams.splice(index, 1);
        this.emitter.emit(new TextListParameter(this.descriptor.ref, this.value));
    }

    add(value: string) {
        if (this._valueParams.length === this.descriptor.max) {
            this.maxElementsReached = true;
            setTimeout(() => this.maxElementsReached = false, 5000);
            return;
        }
        this._valueParams.push(new TextParameter({id: this.generateId()}, value));
        this.newInputRef.clear();
        this.emitter.emit(new TextListParameter(this.descriptor.ref, this.value));
    }

    private drop(event: CdkDragDrop<TextParameter[]>) {
        moveItemInArray(this._valueParams, event.previousIndex, event.currentIndex);
        this.emitter.emit(new TextListParameter(this.descriptor.ref, this.value));
    }

    private generateId(): string {
        return `text-${TextListParameterComponent.refGenerator++}`;
    }


}