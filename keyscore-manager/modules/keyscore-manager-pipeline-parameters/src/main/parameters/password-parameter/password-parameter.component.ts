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
                   (keyup)="validate()"
                   (keyup.enter)="onEnter($event)"
                   [value]="parameter.value">
            <mat-label *ngIf="showLabel">{{label || descriptor.displayName}}</mat-label>
            <button *ngIf="passwordInput.value" mat-button matSuffix mat-icon-button aria-label="Clear"
                    (click)="clear()">
                <mat-icon>close</mat-icon>
            </button>
            <button *ngIf="passwordInput.value" mat-button matSuffix mat-icon-button aria-label="Show-Hide-Password"
                    (mousedown)="showPassword()"
                    (mouseup)="hidePassword()"
                    (mouseout)="hidePassword()">
                <mat-icon>remove_red_eye</mat-icon>
            </button>
        </mat-form-field>
        <div [ngSwitch]="this.warning">
            <p *ngSwitchCase="'WARNING_MANDATORY_BUT_EMPTY'" class="parameter-warn" translate
               [translateParams]="{name:descriptor.displayName}">
                PARAMETER.IS_REQUIRED
            </p>
            <p *ngSwitchCase="'WARNING_TOO_SHORT'" class="parameter-warn" translate
               [translateParams]="{value:descriptor.minLength}">
                PARAMETER.PASSWORD_LENGTH_TOO_SHORT
            </p>
            <p *ngSwitchCase="'WARNING_TOO_LONG'" class="parameter-warn" translate
               [translateParams]="{value:descriptor.maxLength}">
                PARAMETER.PASSWORD_LENGTH_TOO_LONG
            </p>
            <p *ngSwitchCase="'WARNING_INVALID'" class="parameter-warn" translate
               [translateParams]="{pattern:descriptor.validator.expression}">
                PARAMETER.FULFILL_PATTERN
            </p>
        </div>
    `,
    styleUrls: ['../../style/parameter-module-style.scss']
})
export class PasswordParameterComponent extends ParameterComponent<PasswordParameterDescriptor, PasswordParameter> {

    @Input() showLabel: boolean = true;

    @Input()
    get value(): PasswordParameter {
        return new PasswordParameter(this.descriptor.ref, this.passwordInputRef.nativeElement.value);
    }

    @ViewChild('passwordInput', { static: true }) passwordInputRef: ElementRef;

    isPasswordVisible: boolean = false;
    warning: string = null;

    constructor(private stringValidator: StringValidatorService) {
        super();
    }

    clear() {
        this.passwordInputRef.nativeElement.value = "";
        this.onChange();
    }

    focus(event: Event) {
        this.passwordInputRef.nativeElement.focus();
    }

    onChange(): void {
        if (this.validate()) {
            this.emit(this.value);
        }
    }

    onEnter(event: Event): void {
        this.keyUpEnterEvent.emit(event);
    }

    showPassword(): void {
        this.isPasswordVisible = true;
    }

    hidePassword(): void {
        this.isPasswordVisible = false;
    }

    validate(): boolean {

        if (this.isMandatoryButEmpty()) {
            this.warning = "WARNING_MANDATORY_BUT_EMPTY";
            return false;
        }

        if (this.isTooShort()) {
            this.warning = "WARNING_TOO_SHORT";
            return false;
        }

        if (this.isTooLong()) {
            this.warning = "WARNING_TOO_LONG";
            return false;
        }

        if (this.isInvalid()) {
            this.warning = "WARNING_INVALID";
            return false;
        }

        this.warning = null;
        return true;
    }

    private isMandatoryButEmpty(): boolean {
        return this.descriptor.mandatory && (!this.value.value || this.value.value.length < 1)
    }

    private isTooShort(): boolean {
        return this.value.value.length < this.descriptor.minLength;
    }

    private isTooLong(): boolean {
        if (this.descriptor.maxLength === 0) return false;
        return this.value.value.length > this.descriptor.maxLength;
    }

    private isInvalid(): boolean {

        if (!this.descriptor.validator) {
            return false;
        }

        return !this.stringValidator.validate(this.value.value, this.descriptor.validator);
    }
}
