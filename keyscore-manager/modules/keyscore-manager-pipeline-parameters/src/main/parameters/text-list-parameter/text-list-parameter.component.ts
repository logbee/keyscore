import {Component, ElementRef, ViewChild} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {TextListParameter, TextListParameterDescriptor} from "./text-list-parameter.model";
import {TextParameter} from "../text-parameter/text-parameter.model";
import {animate, animateChild, query, stagger, style, transition, trigger} from "@angular/animations";
import {TextParameterComponent} from "../text-parameter/text-parameter.component";

@Component({
    selector: 'parameter-text-list',
    template: `
        <div fxLayout="row-reverse">
            <button mat-button mat-icon-button (click)="add(newInput.value)" fxFlexAlign="center">
                <mat-icon color="accent">add</mat-icon>
            </button>
            <parameter-text #newInput fxFlex
                            [descriptor]="descriptor"
                            [parameter]="_newParameter" (parameter)="add($event.value)">
            </parameter-text>
        </div>
        <div @items *ngFor="let param of _mockParams;let i=index" fxLayout="row-reverse">
            <button mat-button mat-icon-button (click)="remove(i)" fxFlexAlign="center">
                <mat-icon color="warn">delete</mat-icon>
            </button>
            <parameter-text fxFlex [showLabel]="false"
                            [descriptor]="descriptor.descriptor"
                            [parameter]="param" (parameter)="onChange($event,i)">
            </parameter-text>
        </div>

    `,
    animations: [
        trigger('items', [
            transition(':enter', [
                style({transform: 'scale(0.5)', opacity: 0}),  // initial
                animate('1s cubic-bezier(.8, -0.6, 0.2, 1.5)',
                    style({transform: 'scale(1)', opacity: 1}))  // final
            ]),
            transition(':leave', [
                style({transform: 'scale(1)', opacity: 1, height: '*'}),
                animate('1s cubic-bezier(.8, -0.6, 0.2, 1.5)',
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
    private _mockParams: TextParameter[] = [];

    get value() {
        return this._mockParams.map(param => param.value);
    }

    onInit() {
        this.parameter.value.forEach((val, index) => {
            this._mockParams.push(new TextParameter({id: this.generateId()}, val))
        });
        console.log("mock Params: ", this._mockParams);
    }

    onChange(textParameter: TextParameter, index: number) {
        if (index >= 0) {
            this._mockParams.splice(index, 1, new TextParameter({id: textParameter.ref.id}, textParameter.value));
            this.emitter.emit(new TextListParameter(this.descriptor.ref, this.value));
        }
    }

    remove(index: number) {
        this._mockParams.splice(index, 1);
        this.emitter.emit(new TextListParameter(this.descriptor.ref, this.value));
    }

    add(value: string) {
        this._mockParams.splice(0, 0, new TextParameter({id: this.generateId()}, value));
        //this.newInputRef.clear();
    }

    private generateId(): string {
        return `text-${TextListParameterComponent.refGenerator++}`;
    }

}