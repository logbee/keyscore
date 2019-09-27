import {Component, ElementRef, Input, ViewChild} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {StringValidatorService} from "../../service/string-validator.service";
import {
    PasswordParameter,
    PasswordParameterDescriptor
} from "@/../modules/keyscore-manager-models/src/main/parameters/password-parameter.model";

@Component({
    selector: `parameter-password`,
    template: `
        <mat-form-field>
            <input #passwordInput matInput [type]="isPasswordVisible ? 'text' : 'password'"
                   (change)="onChange()"
                   (keyup.enter)="onEnter($event)"
                   [value]="parameter.value">
            <mat-label *ngIf="showLabel">{{label || descriptor.displayName}}</mat-label>
            <button *ngIf="passwordInput.value" mat-button matSuffix mat-icon-button aria-label="Clear" (click)="clear()">
                <mat-icon>close</mat-icon>
            </button>
            <button *ngIf="passwordInput.value" mat-button matSuffix mat-icon-button aria-label="Show-Hide-Password"
                (mousedown)="showPassword()"
                (mouseup)="hidePassword()"
                (mouseout)="hidePassword()">
                <mat-icon>remove_red_eye</mat-icon>
            </button>
        </mat-form-field>
        <p class="parameter-warn" *ngIf="descriptor.mandatory && !passwordInput.value"
           translate [translateParams]="{name:descriptor.displayName}">
            PARAMETER.IS_REQUIRED
        </p>
        <p class="parameter-warn" *ngIf="!isValid(passwordInput.value) && descriptor.validator.description">
            {{descriptor.validator.description}}</p>
        <p class="parameter-warn" *ngIf="!isValid(passwordInput.value) && !descriptor.validator.description"
           translate [translateParams]="{pattern:descriptor.validator.expression}">
            PARAMETER.FULFILL_PATTERN
        </p>
    `,
    styleUrls:['../../style/parameter-module-style.scss']
})
export class PasswordParameterComponent extends ParameterComponent<PasswordParameterDescriptor, PasswordParameter> {

    @Input() showLabel: boolean = true;

    @Input()
    get value():PasswordParameter{
        return new PasswordParameter(this.descriptor.ref,this.passwordInputRef.nativeElement.value);
    }

    @ViewChild('passwordInput') passwordInputRef:ElementRef;

    private isPasswordVisible: boolean = false;

    constructor(private stringValidator: StringValidatorService) {
        super();
    }

    public clear(){
        this.passwordInputRef.nativeElement.value="";
        this.onChange();
    }

    public focus(event:Event){
        this.passwordInputRef.nativeElement.focus();
    }

    private onChange(): void {
        this.emit(this.value);
    }

    private onEnter(event:Event): void {
        this.keyUpEnterEvent.emit(event);
    }

    private isValid(value: string): boolean {
        if (!this.descriptor.validator) {
            return true;
        }
        return this.stringValidator.validate(value, this.descriptor.validator);
    }

    private showPassword(): void {
        this.isPasswordVisible = true;
    }

    private hidePassword(): void {
        this.isPasswordVisible = false;
    }
}
