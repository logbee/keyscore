import {Component, ElementRef, forwardRef, Input, OnInit, ViewChild} from "@angular/core";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {Parameter} from "../../models/parameters/Parameter";
import {
    FieldNameListParameterDescriptor,
    FieldNameParameterDescriptor,
    ResolvedParameterDescriptor
} from "../../models/parameters/ParameterDescriptor";

@Component({
    selector: "field-parameter-list",
    template:
            `
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-form-field>
                <input #addItemInput matInput type="text" placeholder="{{'PARAMETERLISTCOMPONENT.NAMEOFFIELD' | translate}}" [matAutocomplete]="auto">
            </mat-form-field>tada
            <button mat-icon-button color="accent" (click)="addItem(addItemInput.value)">
                <mat-icon>add_circle_outline</mat-icon>
            </button>
        </div>
        <div fxLayout="row" fxLayoutGap="15px">
            <mat-hint *ngIf="fieldNameEmpty" style="color: rgba(204,16,8,0.65);">
                {{'PARAMETERLISTCOMPONENT.NAMEREQUIRED' | translate}}
            </mat-hint>
            <mat-hint *ngIf="duplicate" style="color: rgba(204,16,8,0.65);">
                {{'PARAMETERLISTCOMPONENT.NAMEDUPLICATE' | translate}}
            </mat-hint>
        </div>
        <div (click)="onTouched()" *ngIf="parameterValues.length > 0">
            <div style="display: inline-block; margin: 10px"
                 *ngFor="let value of parameterValues;index as i">
                <mat-chip-list class="mat-chip-list-stacked">
                    <mat-chip [color]="'accent'">
                        {{value}}
                        <mat-icon class="badge-icon cursor-pointer" (click)="removeItem(i)">close</mat-icon>
                    </mat-chip>
                </mat-chip-list>
            </div>
        </div>
        
        <!--Autocompletion-->
        <mat-autocomplete #auto="matAutocomplete">
            <mat-option *ngFor="let field of hints" [value]="field">{{field}}</mat-option>
        </mat-autocomplete>
        
    `,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FieldParameterList),
            multi: true
        }
    ]
})

export class FieldParameterList implements ControlValueAccessor, OnInit {

    @Input() public disabled = false;
    @Input() public distinctValues = true;
    @Input() public parameter: Parameter;
    @Input() public descriptor: ResolvedParameterDescriptor;
    @ViewChild('addItemInput') inputField: ElementRef;

    public parameterValues: string[] = [];
    private duplicate: boolean;
    private fieldNameEmpty: boolean;
    private hints: string[] = ['default'];
    private hint = undefined;
    public onChange = (elements: string[]) => {
        return;
    };

    public onTouched = () => {
        return;
    };

    public ngOnInit(): void {
        this.hint = (this.descriptor as FieldNameListParameterDescriptor).descriptor.hint;
        console.log(this.hint);
        switch (this.hint) {
            case 'PresentField':
                this.hints = ['fieldsfromDataset'];
                break;
            case 'AbsentField':
                break;
            case 'AnyField':
                break;
        }
        this.parameterValues = [...this.parameter.value];
    }

    public writeValue(elements: string[]): void {

        this.parameterValues = elements;
        this.onChange(elements);

    }

    public registerOnChange(f: (elements: string[]) => void): void {
        this.onChange = f;
    }

    public registerOnTouched(f: () => void): void {
        this.onTouched = f;
    }

    public setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    get value(): string[] {
        return [...this.parameterValues];
    }

    public removeItem(index: number) {
        const newValues = [...this.parameterValues];
        newValues.splice(index, 1);
        this.writeValue(newValues);
    }

    public addItem(value: string) {
        if (value) {
            if (!this.distinctValues || (this.distinctValues && !this.parameterValues.some((x) => x === value))) {
                this.duplicate = false;
                this.fieldNameEmpty = false;
                const newValues = [...this.parameterValues];
                newValues.push(value);
                this.writeValue(newValues);
                this.inputField.nativeElement.value = '';
            } else {
                this.duplicate = true;
            }
        } else {
            this.fieldNameEmpty = true;
        }

    }
}
