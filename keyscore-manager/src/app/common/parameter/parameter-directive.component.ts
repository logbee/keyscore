import {Component, forwardRef, Input, OnInit} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {Parameter, ParameterJsonClass} from "../../models/parameters/Parameter";
import {FieldNameHint, ResolvedParameterDescriptor} from "../../models/parameters/ParameterDescriptor";
import {BehaviorSubject} from "rxjs/index";
import {Dataset} from "../../models/dataset/Dataset";
import {DirectiveConfiguration, FieldDirectiveSequenceConfiguration} from "../../models/common/Configuration";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {generateRef} from "../../models/common/Ref";

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
                    <button mat-icon-button color="accent" (click)="addDirective(fieldDirectiveSequence)">
                        <mat-icon matTooltip="Add a new directive to this field">add_circle_outline</mat-icon>
                    </button>
                </div>
                <mat-divider class="padding-bottom-5"></mat-divider>
                <div cdkDropList class="field-directive-sequence" (cdkDropListDropped)="dropFieldDirective($event)">
                    <div class="field-directives-box" *ngFor="let fieldDirective of fieldDirectiveSequence.directives"
                         cdkDrag>
                        {{fieldDirective.ref.uuid}}
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
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;

    @Input('datasets') set datasets(data: Dataset[]) {
        this.datasets$.next(data);
    }

    private datasets$: BehaviorSubject<Dataset[]> = new BehaviorSubject<Dataset[]>([]);

    public fieldNameHint: typeof FieldNameHint = FieldNameHint;

    public parameterValues: FieldDirectiveSequenceConfiguration[] = [];

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
        let fieldDirectiveSequence: FieldDirectiveSequenceConfiguration = {
            fieldName: value,
            directives: []
        };
        newValues.push(fieldDirectiveSequence);
        this.writeValue(newValues);
    }

    //Just for testing
    public addDirective(sequence: FieldDirectiveSequenceConfiguration) {
        let currentSeqIndex = this.parameterValues.findIndex(seq => seq.fieldName === sequence.fieldName);
        let newValues = [...this.parameterValues];
        newValues[currentSeqIndex].directives.push({
            ref: generateRef(),
            parameters: {jsonClass: ParameterJsonClass.ParameterSet, parameters: []}
        });
        this.writeValue(newValues);
    }

    public removeDirectiveSequence(sequence: FieldDirectiveSequenceConfiguration) {

    }

    public dropFieldDirectiveSequence(event: CdkDragDrop<FieldDirectiveSequenceConfiguration[]>) {
        let newValues = [...this.parameterValues];
        moveItemInArray(newValues, event.previousIndex, event.currentIndex);
        this.writeValue(newValues);

    }

    public dropFieldDirective(event: CdkDragDrop<DirectiveConfiguration[]>) {

    }
}
