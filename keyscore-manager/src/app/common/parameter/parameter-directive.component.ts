import {Component, forwardRef, Input, OnInit} from "@angular/core";
import {ControlValueAccessor, FormGroup, NG_VALUE_ACCESSOR} from "@angular/forms";
import {Parameter, ParameterJsonClass} from "../../models/parameters/Parameter";
import {
    FieldDirectiveSequenceParameterDescriptor,
    FieldNameHint,
    ResolvedFieldDirectiveDescriptor,
    ResolvedParameterDescriptor
} from "../../models/parameters/ParameterDescriptor";
import {BehaviorSubject} from "rxjs/index";
import {Dataset} from "../../models/dataset/Dataset";
import {DirectiveConfiguration, FieldDirectiveSequenceConfiguration} from "../../models/common/Configuration";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {ParameterControlService} from "./service/parameter-control.service";
import {parameterDescriptorToParameter, zip} from "../../util";
import uuid = require("uuid");
import {generateRef} from "../../models/common/Ref";

@Component({
    selector: "parameter-directive",
    template:
            `
        <div fxLayout="row" fxLayoutAlign="space-between center">
            <div>Directives</div>
            <button mat-icon-button color="accent" (click)="addField()">
                <mat-icon>add_circle_outline</mat-icon>
            </button>
        </div>
        <div cdkDropList class="field-directive-sequence" (cdkDropListDropped)="dropFieldDirectiveSequence($event)">
            <div class="field-directives-box"
                 *ngFor="let fieldDirectiveSequence of parameterValues;index as sequenceIndex" cdkDrag>
                <div fxLayout="row" fxLayoutAlign="space-between center">
                    <button mat-icon-button color="warn" (click)="removeDirectiveSequence(sequenceIndex)">
                        <mat-icon matTooltip="Remove all directives for this field.">remove_circle_outline</mat-icon>
                    </button>
                    <div>
                        <auto-complete-input
                                [datasets]="datasets$ | async"
                                [hint]="fieldNameHint.PresentField"
                                [parameterDescriptor]="parameterDescriptor"
                                [labelText]="'Field'"
                                [parameter]="parameter"
                                [inputValue]="fieldDirectiveSequence.fieldName"
                                (onChangeEmit)="onFieldNameChange($event,sequenceIndex)"

                        >

                        </auto-complete-input>
                    </div>
                    <mat-menu #directiveMenu>
                        <button fxLayout="row" fxLayoutAlign="space-between center" mat-menu-item
                                *ngFor="let directiveDescriptor of parameterDescriptor.directives"
                                (click)="addDirective(fieldDirectiveSequence,directiveDescriptor,sequenceIndex)">
                            <mat-icon>add_circle_outline</mat-icon>
                            {{directiveDescriptor.info.displayName}}
                        </button>
                    </mat-menu>
                    <button mat-icon-button color="accent" [matMenuTriggerFor]="directiveMenu">
                        <mat-icon matTooltip="Add a new directive to this field">add_circle_outline</mat-icon>
                    </button>
                </div>
                <mat-divider class="padding-bottom-5"></mat-divider>
                <div cdkDropList class="field-directive-sequence"
                     (cdkDropListDropped)="dropFieldDirective($event,sequenceIndex)" fxLayout="column">
                    <div class="field-directives-box"
                         *ngFor="let fieldDirective of fieldDirectiveSequence.directives;index as directiveIndex"
                         cdkDrag>
                        <div fxLayout="row" fxLayoutAlign="start center" fxLayoutGap="10">
                            <button mat-icon-button color="warn"
                                    (click)="removeDirective(sequenceIndex,directiveIndex)">
                                <mat-icon matTooltip="Remove this directive.">remove_circle_outline</mat-icon>
                            </button>
                            <div>{{getFieldDirectiveDescriptor(fieldDirective).info.displayName}}</div>
                            <div></div>
                        </div>
                        <mat-divider
                                *ngIf="getKeys(directiveParameterMappings.get(fieldDirective.instance.uuid)).length"></mat-divider>
                        <form [formGroup]="directiveFormGroups.get(fieldDirective.instance.uuid)"
                              class="directive-form">
                            <app-parameter
                                    *ngFor="let parameter of getKeys(directiveParameterMappings.get(fieldDirective.instance.uuid))"
                                    [parameter]="parameter"
                                    [parameterDescriptor]="directiveParameterMappings.get(fieldDirective.instance.uuid).get(parameter)"
                                    [form]="directiveFormGroups.get(fieldDirective.instance.uuid)"
                                    [datasets]="datasets$ | async"
                                    [directiveInstance]="fieldDirective.instance.uuid"
                            >
                            </app-parameter>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    `,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => ParameterDirectiveComponent),
            multi: true
        }
    ]
})


export class ParameterDirectiveComponent implements ControlValueAccessor, OnInit {

    @Input() public disabled = false;
    @Input() public parameter: Parameter;
    @Input() public parameterDescriptor: FieldDirectiveSequenceParameterDescriptor;

    @Input('datasets') set datasets(data: Dataset[]) {
        this.datasets$.next(data);
    }

    private datasets$: BehaviorSubject<Dataset[]> = new BehaviorSubject<Dataset[]>([]);

    public fieldNameHint: typeof FieldNameHint = FieldNameHint;

    public parameterValues: FieldDirectiveSequenceConfiguration[] = [];

    public directiveFormGroups: Map<string, FormGroup> = new Map();
    public directiveParameterMappings: Map<string, Map<Parameter, ResolvedParameterDescriptor>> = new Map();

    public constructor(private parameterService: ParameterControlService) {

    }

    public onChange = (elements: FieldDirectiveSequenceConfiguration[]) => {
        undefined;
    };

    public onTouched = () => {
        undefined;
    };

    public ngOnInit(): void {
        if (this.parameter.jsonClass === ParameterJsonClass.FieldDirectiveSequenceParameter) {
            this.parameterValues = [...this.parameter.value as FieldDirectiveSequenceConfiguration[]];
            this.parameterValues.map((sequence, seqIndex) => sequence.directives.forEach((directive, directiveIndex) =>
                this.createDirectiveSubForm(this.parameterValues, seqIndex, directiveIndex, this.parameterDescriptor.directives.find(dir => dir.ref.uuid === directive.ref.uuid), sequence)))

        }
        else {
            console.error("Passed the wrong parameter type into the parameter-directive.component. Parametertype is: "
                + this.parameter.jsonClass + ". But it should be: " + ParameterJsonClass.FieldDirectiveSequenceParameter);
        }

    }

    public writeValue(elements: FieldDirectiveSequenceConfiguration[]): void {

        this.parameterValues = elements;
        this.onChange(elements);

    }

    public registerOnChange(f: (elements: FieldDirectiveSequenceConfiguration[]) => void): void {
        this.onChange = f;
    }

    public registerOnTouched(f: () => void): void {
        this.onTouched = f;
    }

    public setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    get value(): FieldDirectiveSequenceConfiguration[] {
        return [...this.parameterValues];
    }

    public removeDirective(seqIndex: number, directiveIndex: number) {
        const newValues = [...this.parameterValues];
        if (seqIndex !== -1) {
            newValues[seqIndex].directives.splice(directiveIndex, 1);
            this.writeValue(newValues);
        }
    }

    public addField() {
        let newValues = [...this.parameterValues];

        let fieldDirectiveSequence: FieldDirectiveSequenceConfiguration = {
            fieldName: "",
            directives: [],

        };
        newValues.push(fieldDirectiveSequence);
        this.writeValue(newValues);

    }

    public addDirective(sequence: FieldDirectiveSequenceConfiguration, directive: ResolvedFieldDirectiveDescriptor, seqIndex: number) {
        let newValues = [...this.parameterValues];
        newValues[seqIndex].directives.push({
            ref: directive.ref,
            instance: generateRef(),
            parameters: {
                jsonClass: ParameterJsonClass.ParameterSet,
                parameters: directive.parameters.map(parameter =>
                    parameterDescriptorToParameter(parameter))
            }
        });
        const index = newValues[seqIndex].directives.length - 1;
        this.createDirectiveSubForm(newValues, seqIndex, index, directive, sequence);
        this.writeValue(newValues);
    }


    private createDirectiveSubForm(values, currentSeqIndex, index, directive: ResolvedFieldDirectiveDescriptor, sequence: FieldDirectiveSequenceConfiguration) {
        let parameterMapping: Map<Parameter, ResolvedParameterDescriptor> = new Map(zip([values[currentSeqIndex].directives[index].parameters.parameters, directive.parameters]));
        this.directiveParameterMappings.set(sequence.directives[index].instance.uuid, parameterMapping);
        let form = this.parameterService.toFormGroup(parameterMapping, sequence.directives[index].instance.uuid);
        let directiveConfiguration = this.parameterValues[currentSeqIndex].directives[index];
        form.valueChanges.subscribe(values => {
            let instanceRef = Object.keys(values)[0].substring(0, 36);
            let newValues = [...this.parameterValues];
            let currentSeqIndex = newValues.findIndex(seq => seq.fieldName === sequence.fieldName);
            let index = newValues[currentSeqIndex].directives.findIndex(dir => dir.instance.uuid === instanceRef);
            newValues[currentSeqIndex].directives[index].parameters.parameters.forEach(parameter => parameter.value = values[instanceRef + ':' + parameter.ref.id]);
            this.writeValue(newValues);

        });
        this.directiveFormGroups.set(sequence.directives[index].instance.uuid, form);
    }

    public removeDirectiveSequence(index: number) {
        const newValues = [...this.parameterValues];
        newValues.splice(index, 1);
        this.writeValue(newValues);
    }

    public dropFieldDirectiveSequence(event: CdkDragDrop<FieldDirectiveSequenceConfiguration[]>) {
        let newValues = [...this.parameterValues];
        moveItemInArray(newValues, event.previousIndex, event.currentIndex);
        this.writeValue(newValues);

    }

    public dropFieldDirective(event: CdkDragDrop<DirectiveConfiguration[]>, sequenceIndex: number) {
        let newValues = [...this.parameterValues];
        moveItemInArray(newValues[sequenceIndex].directives, event.previousIndex, event.currentIndex);
        this.writeValue(newValues);
    }

    public getFieldDirectiveDescriptor(directive: DirectiveConfiguration): ResolvedFieldDirectiveDescriptor {
        return this.parameterDescriptor.directives.find(dir => dir.ref.uuid === directive.ref.uuid);
    }

    public onFieldNameChange(fieldName: string, seqIndex: number,) {
        let newValues = [...this.parameterValues];
        newValues[seqIndex].fieldName = fieldName;
        this.writeValue(newValues);
    }

    getKeys(map: Map<any, any>): any[] {
        return Array.from(map.keys());
    }
}
