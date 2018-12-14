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

@Component({
    selector: "parameter-directive",
    template:
            `
        <div fxLayout="row" fxLayoutGap="15px">
            <auto-complete-input #addFieldInput
                                 [datasets]="datasets$ | async"
                                 [hint]="fieldNameHint.PresentField"
                                 [parameterDescriptor]="parameterDescriptor"
                                 [parameter]="parameter">

            </auto-complete-input>
            <button mat-icon-button color="accent" (click)="addField(addFieldInput.value)">
                <mat-icon>add_circle_outline</mat-icon>
            </button>
        </div>
        <div cdkDropList class="field-directive-sequence" (cdkDropListDropped)="dropFieldDirectiveSequence($event)">
            <div class="field-directives-box" *ngFor="let fieldDirectiveSequence of parameterValues" cdkDrag>
                <div fxLayout="row" fxLayoutAlign="space-between center">
                    <button mat-icon-button color="warn" (click)="removeDirectiveSequence(fieldDirectiveSequence)">
                        <mat-icon matTooltip="Remove all directives for this field.">remove_circle_outline</mat-icon>
                    </button>
                    <div>Fieldname: {{fieldDirectiveSequence.fieldName}}</div>
                    <mat-menu #directiveMenu>
                        <button fxLayout="row" fxLayoutAlign="space-between center" mat-menu-item
                                *ngFor="let directiveDescriptor of parameterDescriptor.directives"
                                (click)="addDirective(fieldDirectiveSequence,directiveDescriptor)">
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
                     (cdkDropListDropped)="dropFieldDirective($event,fieldDirectiveSequence)">
                    <div class="field-directives-box" *ngFor="let fieldDirective of fieldDirectiveSequence.directives"
                         cdkDrag>
                        <div>{{getFieldDirectiveDescriptor(fieldDirective).info.displayName}}</div>
                        <mat-divider></mat-divider>
                        <form [formGroup]="directiveFormGroups.get(getFieldDirectiveDescriptor(fieldDirective).ref.uuid)">
                            <app-parameter
                                    *ngFor="let parameter of getKeys(directiveParameterMappings.get(getFieldDirectiveDescriptor(fieldDirective).ref.uuid))"
                                    [parameter]="parameter"
                                    [parameterDescriptor]="directiveParameterMappings.get(getFieldDirectiveDescriptor(fieldDirective).ref.uuid).get(parameter)"
                                    [form]="directiveFormGroups.get(getFieldDirectiveDescriptor(fieldDirective).ref.uuid)"
                                    [datasets]="datasets$ | async">
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
    public directiveParameterMappings: Map<string,Map<Parameter,ResolvedParameterDescriptor>> = new Map();

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

    public removeItem(index: number) {
        const newValues = [...this.parameterValues];
        newValues.splice(index, 1);
        this.writeValue(newValues);
    }

    public addField(value: string) {
        let newValues = [...this.parameterValues];
        if (newValues.findIndex(directiveSeq => directiveSeq.fieldName === value) === -1) {
            let fieldDirectiveSequence: FieldDirectiveSequenceConfiguration = {
                fieldName: value,
                directives: []
            };
            newValues.push(fieldDirectiveSequence);
            this.writeValue(newValues);
        }
    }

    //Just for testing
    public addDirective(sequence: FieldDirectiveSequenceConfiguration, directive: ResolvedFieldDirectiveDescriptor) {
        let newValues = [...this.parameterValues];
        let currentSeqIndex = newValues.findIndex(seq => seq.fieldName === sequence.fieldName);
        newValues[currentSeqIndex].directives.push({
            ref: directive.ref,
            parameters: {
                jsonClass: ParameterJsonClass.ParameterSet,
                parameters: directive.parameters.map(parameter =>
                    parameterDescriptorToParameter(parameter))
            }
        });
        const index = newValues[currentSeqIndex].directives.length - 1;
        let parameterMapping: Map<Parameter, ResolvedParameterDescriptor> = new Map(zip([newValues[currentSeqIndex].directives[index].parameters.parameters, directive.parameters]));
        this.directiveParameterMappings.set(directive.ref.uuid,parameterMapping);
        let form = this.parameterService.toFormGroup(parameterMapping);
        /*form.valueChanges.subscribe(values => {
            let newValues = [...this.parameterValues];
            let currentSeqIndex = newValues.findIndex(seq => seq.fieldName === sequence.fieldName);
            let index = newValues[currentSeqIndex].directives.findIndex(dir => dir.ref.uuid === directive.ref.uuid);
            newValues[currentSeqIndex].directives[index].parameters.parameters
        })*/
        this.directiveFormGroups.set(directive.ref.uuid, form);
        this.writeValue(newValues);
    }

    public removeDirectiveSequence(sequence: FieldDirectiveSequenceConfiguration) {

    }

    public dropFieldDirectiveSequence(event: CdkDragDrop<FieldDirectiveSequenceConfiguration[]>) {
        let newValues = [...this.parameterValues];
        moveItemInArray(newValues, event.previousIndex, event.currentIndex);
        this.writeValue(newValues);

    }

    public dropFieldDirective(event: CdkDragDrop<DirectiveConfiguration[]>, sequence: FieldDirectiveSequenceConfiguration) {
        let newValues = [...this.parameterValues];
        let index = newValues.findIndex(seq => seq.fieldName === sequence.fieldName);
        moveItemInArray(newValues[index].directives, event.previousIndex, event.currentIndex);
        this.writeValue(newValues);
    }

    public getFieldDirectiveDescriptor(directive: DirectiveConfiguration): ResolvedFieldDirectiveDescriptor {
        return this.parameterDescriptor.directives.find(dir => dir.ref.uuid === directive.ref.uuid);
    }

    getKeys(map: Map<any, any>): any[] {
        return Array.from(map.keys());
    }
}
