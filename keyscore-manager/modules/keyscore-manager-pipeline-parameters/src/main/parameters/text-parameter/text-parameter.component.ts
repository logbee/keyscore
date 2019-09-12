import {Component, ElementRef, EventEmitter, Input, Output, ViewChild} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {StringValidatorService} from "../../service/string-validator.service";
import {TextParameterDescriptor, TextParameter} from "@/../modules/keyscore-manager-models/src/main/parameters/text-parameter.model";

@Component({
    selector: `parameter-text`,
    template: `
        <mat-form-field>
            <input #textInput matInput type="text"
                   (change)="onChange()"
                   (keyup.enter)="onEnter($event)"
                   [value]="parameter.value">
            <mat-label *ngIf="showLabel">{{label || descriptor.displayName}}</mat-label>

            <button mat-button *ngIf="textInput.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="clear()">
                <mat-icon>close</mat-icon>
            </button>
        </mat-form-field>
        <p class="parameter-warn" *ngIf="descriptor.mandatory && !textInput.value">{{descriptor.displayName}} is
            required!</p>
        <p class="parameter-warn" *ngIf="!isValid(textInput.value) && descriptor.validator.description">
            {{descriptor.validator.description}}</p>
        <p class="parameter-warn" *ngIf="!isValid(textInput.value) && !descriptor.validator.description">Your Input has
            to fulfill the following Pattern:
            {{descriptor.validator.expression}}</p>
    `,
    styleUrls:['../../style/parameter-module-style.scss']


})
export class TextParameterComponent extends ParameterComponent<TextParameterDescriptor, TextParameter> {
    @Input() showLabel: boolean = true;
    @Input()
    get value():TextParameter{
        return new TextParameter(this.descriptor.ref,this.textInputRef.nativeElement.value);
    }

    @ViewChild('textInput') textInputRef:ElementRef;


    constructor(private stringValidator: StringValidatorService) {
        super();
    }

    public clear(){
        this.textInputRef.nativeElement.value="";
        this.onChange();
    }

    private onChange(): void {
        this.emit(this.value);
    }

    private onEnter(event:Event): void{
        this.keyUpEnterEvent.emit(event);
    }

    private isValid(value: string): boolean {
        if (!this.descriptor.validator) {
            return true;
        }
        return this.stringValidator.validate(value, this.descriptor.validator);
    }

}