import {Component, ComponentFactoryResolver, ComponentRef, ElementRef, Type, ViewChild} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {StringValidatorService} from "../../service/string-validator.service";
import {FieldParameter, FieldParameterDescriptor} from "./field-parameter.model";
import {Field, Value} from "../../models/value.model";
import {ValueDirective} from "../../value-controls/directives/value.directive";
import {ValueComponentRegistryService} from "../../value-controls/services/value-component-registry.service";
import {ValueComponent} from "../../value-controls/value-component.interface";
import {Subscription} from "rxjs";

@Component({
    selector: `parameter-field`,
    template: `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field fxFlex="40">
                <input #fieldInput matInput type="text" [placeholder]="descriptor.defaultValue"
                       (change)="onChange($event.target.value)"
                       [value]="parameter.value">
                <mat-label>{{descriptor.displayName}}</mat-label>

                <button mat-button *ngIf="fieldInput.value" matSuffix mat-icon-button aria-label="Clear"
                        (click)="fieldInput.value='';onChange('')">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>
            <div fxFlex>
                <ng-template value-host></ng-template>
            </div>
        </div>
        <p class="parameter-required" *ngIf="descriptor.mandatory && !fieldInput.value">{{descriptor.displayName}} is
            required!</p>
        <p class="parameter-warn" *ngIf="!isValid(fieldInput.value) && descriptor.nameValidator.description">
            {{descriptor.nameValidator.description}}</p>
        <p class="parameter-warn" *ngIf="!isValid(fieldInput.value) && !descriptor.nameValidator.description">Your Input
            has to fulfill the following Pattern:
            {{descriptor.nameValidator.expression}}</p>
    `,

})
export class FieldParameterComponent extends ParameterComponent<FieldParameterDescriptor, FieldParameter> {

    @ViewChild(ValueDirective) valueHost: ValueDirective;
    @ViewChild('fieldInput') fieldInputRef: ElementRef;

    private valueComponentInstance: ComponentRef<ValueComponent>;
    private subs: Subscription[] = [];

    constructor(private stringValidator: StringValidatorService,
                private valueRegistry: ValueComponentRegistryService,
                private componentFactoryResolver: ComponentFactoryResolver) {
        super();
    }

    onInit() {
        this.loadValueComponent();

        this.subs.push(this.valueComponentInstance.instance.changed.subscribe(_ => {
            this.onChange(this.fieldInputRef.nativeElement.value)
        }))
    }

    private onChange(fieldName: string): void {
        const parameter = new FieldParameter(this.descriptor.ref, new Field(fieldName, this.valueComponentInstance.instance.value));
        console.log("changed: ", parameter);
        this.emit(parameter);
    }

    private isValid(value: string): boolean {
        if (!this.descriptor.nameValidator) {
            return true;
        }
        return this.stringValidator.validate(value, this.descriptor.nameValidator);
    }

    private loadValueComponent() {
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(this.valueRegistry.getValueComponent(this.descriptor.fieldValueType));
        this.valueHost.viewContainerRef.clear();
        this.valueComponentInstance = this.valueHost.viewContainerRef.createComponent(componentFactory);
    }

    onDestroy() {
        this.subs.forEach(sub => sub.unsubscribe());
    }
}