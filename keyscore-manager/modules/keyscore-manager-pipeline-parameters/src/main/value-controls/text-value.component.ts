import {Component, EventEmitter, HostBinding, Input, OnDestroy, Output} from "@angular/core";
import {FormControl} from "@angular/forms";
import {coerceBooleanProperty} from "@angular/cdk/coercion";
import {ValueComponent} from "./value-component.interface";
import {TextValue, MimeType} from "@/../modules/keyscore-manager-models/src/main/dataset/Value";
import {TranslateService} from "@ngx-translate/core";
import {takeUntil} from "rxjs/operators";
import {Subject} from "rxjs";

@Component({
    selector: 'ks-text-value-input',
    template: `
        <mat-form-field>
            <input #inputField matInput type="text" [formControl]="inputControl" (change)="onChange()" (keyup.enter)="keyUpEnter.emit($event)">
            <mat-label *ngIf="showLabel && label">{{label}}</mat-label>
            <mat-label *ngIf="showLabel && !label" translate>PARAMETER.VALUE</mat-label>
            <button mat-button tabindex="-1" *ngIf="inputField.value" matSuffix mat-icon-button aria-label="Clear"
                    (click)="inputControl.setValue('');inputField.focus();onChange( )">
                <mat-icon>close</mat-icon>
            </button>
            <mat-icon matSuffix [inline]="true" svgIcon="text-icon"></mat-icon>
        </mat-form-field>
    `,
    styleUrls:['../style/parameter-module-style.scss']
})
export class TextValueComponent implements ValueComponent{

    static nextId = 0;

    @HostBinding() id = `ks-text-value-input-${TextValueComponent.nextId++}`;
    inputControl = new FormControl();

    @Input()
    get value(): TextValue {
        return new TextValue(this.inputControl.value, new MimeType("", ""));
    }

    set value(val: TextValue) {
        this.inputControl.setValue(val.value);
    }

    @Input()
    get disabled(): boolean {
        return this._disabled;
    }

    set disabled(value: boolean) {
        this._disabled = coerceBooleanProperty(value);
        this._disabled ? this.inputControl.disable() : this.inputControl.enable();
    }

    private _disabled = false;

    @Input() label: string;
    @Input() showLabel:boolean = true;

    @Output() changed: EventEmitter<TextValue> = new EventEmitter<TextValue>();
    @Output() keyUpEnter:EventEmitter<Event> = new EventEmitter<Event>();


    onChange() {
        this.changed.emit(this.value);
    }


}