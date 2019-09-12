import {Component, ComponentFactoryResolver, ComponentRef, Input, ViewChild} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {StringValidatorService} from "../../service/string-validator.service";
import {ValueDirective} from "../../value-controls/directives/value.directive";
import {ValueComponentRegistryService} from "../../value-controls/services/value-component-registry.service";
import {ValueComponent} from "../../value-controls/value-component.interface";
import {Subscription} from "rxjs";
import {AutocompleteFilterComponent} from "../../shared-controls/autocomplete-filter.component";
import {FieldParameterDescriptor, FieldParameter} from "@/../modules/keyscore-manager-models/src/main/parameters/field-parameter.model";
import {Field} from "@/../modules/keyscore-manager-models/src/main/dataset/Field";

@Component({
    selector: `parameter-field`,
    template: `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field fxFlex="30">
                <ks-autocomplete-input #fieldInput
                                       [value]="parameter.value?.name"
                                       [options]="autoCompleteDataList"
                                       (change)="onChange()"
                                       (keyUpEnterEvent)="onEnter($event)">
                </ks-autocomplete-input>
                <mat-label *ngIf="showLabel">Field Name</mat-label>

                <button mat-button *ngIf="fieldInput.value" matSuffix mat-icon-button aria-label="Clear"
                        (click)="clear()">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>
            <div fxFlex="70">
                <ng-template value-host></ng-template>
            </div>
        </div>
        <p class="parameter-warn" *ngIf="descriptor.mandatory && !fieldInput.value">Field Name is
            required!</p>
        <p class="parameter-warn" *ngIf="!isValid(fieldInput.value) && descriptor.nameValidator.description">
            {{descriptor.nameValidator.description}}</p>
        <p class="parameter-warn" *ngIf="!isValid(fieldInput.value) && !descriptor.nameValidator.description">Your Input
            has to fulfill the following Pattern: {{descriptor.nameValidator.expression}}</p>
    `,
    styleUrls:['../../style/parameter-module-style.scss']


})
export class FieldParameterComponent extends ParameterComponent<FieldParameterDescriptor, FieldParameter> {
    @Input() showLabel: boolean = true;
    @ViewChild(ValueDirective) valueHost: ValueDirective;
    @ViewChild('fieldInput') autoCompleteComponent: AutocompleteFilterComponent;

    get value() {
        return new FieldParameter(this.descriptor.ref, new Field(this.autoCompleteComponent.value, this.valueComponentInstance.instance.value));
    }

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
            this.onChange()
        }));

        this.subs.push(this.valueComponentInstance.instance.keyUpEnter.subscribe(event =>
            this.onEnter(event)
        ));
    }

    public clear() {
        this.autoCompleteComponent.value = '';
        this.autoCompleteComponent.focus(null);
        this.onChange();
    }

    private onEnter(event: Event) {
        this.keyUpEnterEvent.emit(event);
    }

    private onChange(): void {
        this.emit(this.value);
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
        if (this.parameter.value && this.parameter.value.value) {
            this.valueComponentInstance.instance.value = this.parameter.value.value;
        }
        this.valueComponentInstance.instance.showLabel = this.showLabel;
    }

    onDestroy() {
        this.subs.forEach(sub => sub.unsubscribe());
    }
}