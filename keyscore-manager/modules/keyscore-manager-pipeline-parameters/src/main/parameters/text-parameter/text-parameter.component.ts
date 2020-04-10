import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {StringValidatorService} from "../../service/string-validator.service";
import {
    TextParameter,
    TextParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/text-parameter.model";

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
        <p class="parameter-warn" *ngIf="descriptor.mandatory && !textInput.value"
        translate [translateParams]="{name:descriptor.displayName}">
            PARAMETER.IS_REQUIRED
        </p>
        <p class="parameter-warn" *ngIf="!isValid(textInput.value) && descriptor.validator.description">
            {{descriptor.validator.description}}</p>
        <p class="parameter-warn" *ngIf="!isValid(textInput.value) && !descriptor.validator.description"
        translate [translateParams]="{pattern:descriptor.validator.expression}">
            PARAMETER.FULFILL_PATTERN
        </p>
    `,
    styleUrls:['../../style/parameter-module-style.scss']
})
export class TextParameterComponent extends ParameterComponent<TextParameterDescriptor, TextParameter> {

    @Input() showLabel: boolean = true;

    @Input()
    get value():TextParameter{
        return new TextParameter(this.descriptor.ref,this.textInputRef.nativeElement.value);
    }

    @ViewChild('textInput', { static: true }) textInputRef:ElementRef;

    constructor(private stringValidator: StringValidatorService) {
        super();
    }

    clear(){
        this.textInputRef.nativeElement.value="";
        this.onChange();
    }

    focus(event:Event){
        this.textInputRef.nativeElement.focus();
    }

    isValid(value: string): boolean {
        if (!this.descriptor.validator) {
            return true;
        }
        return this.stringValidator.validate(value, this.descriptor.validator);
    }

    onChange(): void {
        this.emit(this.value);
    }

    onEnter(event:Event): void {
        this.keyUpEnterEvent.emit(event);
    }

}
