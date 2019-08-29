import {Component, Input, OnInit} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {
    Dataset,
    Parameter,
    ParameterDescriptorJsonClass,
    ParameterGroup,
    ParameterGroupDescriptor,
    ResolvedParameterDescriptor
} from "@keyscore-manager-models";
import "./style/parameter-module-style.scss";
import {BehaviorSubject} from "rxjs/index";

@Component({
    selector: "app-parameter",
    template: `
        <div [ngSwitch]="parameterDescriptor.jsonClass" [formGroup]="form" class="parameter-wrapper">
            <mat-form-field *ngSwitchCase="jsonClass.TextParameterDescriptor">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="directiveInstance || parameter.ref.id"
                       [id]="directiveInstance || parameter.ref.id">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>

                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>
            <mat-form-field *ngSwitchCase="jsonClass.NumberParameterDescriptor">
                <input matInput type="number"
                       [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="directiveInstance || parameter.ref.id"
                       [id]="directiveInstance || parameter.ref.id">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <div *ngSwitchCase="jsonClass.ParameterGroupDescriptor">
                <div *ngIf="form.value[parameterDescriptor.condition.parameter.id] ? !parameterDescriptor.condition.negate
                        : parameterDescriptor.condition.negate">
                    <app-parameter *ngFor="let parameterI of getKeys(parameterGroupMapping)" [parameter]="parameterI"
                                   [parameterDescriptor]="parameterGroupMapping.get(parameterI)"
                                   [form]="form"
                                   [datasets]="datasets$ | async">
                    </app-parameter>
                </div>
            </div>

            <mat-form-field *ngSwitchCase="jsonClass.DecimalParameterDescriptor">
                <input matInput type="number"
                       [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="directiveInstance || parameter.ref.id"
                       [id]="directiveInstance || parameter.ref.id">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>


            <div *ngSwitchCase="jsonClass.BooleanParameterDescriptor"
                 class="toggleCheckbox" [id]="directiveInstance || parameter.ref.id">
                <mat-slide-toggle [checked]="parameter.value"
                                  id="checkbox{{directiveInstance ? directiveInstance : parameter.ref.id}}"
                                  [formControlName]="directiveInstance || parameter.ref.id">
                    {{parameterDescriptor.info.displayName}}
                </mat-slide-toggle>
            </div>

            <mat-form-field *ngSwitchCase="jsonClass.ChoiceParameterDescriptor"
                            [id]="directiveInstance || parameter.ref.id">
                <mat-select [formControlName]="directiveInstance || parameter.ref.id"
                            [placeholder]="parameterDescriptor.defaultValue"
                            [attr.multiple]="parameterDescriptor.max > 1 ? '' :null">
                    <mat-option *ngFor="let choice of parameterDescriptor.choices" [value]="choice.name"
                                [matTooltip]="choice.description" matTooltipPosition="before">{{choice.displayName}}
                    </mat-option>
                </mat-select>
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
            </mat-form-field>

            <mat-form-field *ngSwitchCase="jsonClass.ExpressionParameterDescriptor"
                            [id]="directiveInstance || parameter.ref.id">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="directiveInstance || parameter.ref.id"
                       [id]="directiveInstance || parameter.ref.id">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <auto-complete-input *ngSwitchCase="jsonClass.FieldNameParameterDescriptor"
                                 [id]="directiveInstance || parameter.ref.id"
                                 [parameter]="parameter"
                                 [datasets]="datasets$ |async"
                                 [hint]="parameterDescriptor.hint ? parameterDescriptor.hint : ''"
                                 [parameterDescriptor]="parameterDescriptor"
                                 [formControlName]="directiveInstance || parameter.ref.id">

            </auto-complete-input>

            <parameter-fieldnamepattern *ngSwitchCase="jsonClass.FieldNamePatternParameterDescriptor"
                                        [id]="directiveInstance || parameter.ref.id"
                                        [parameter]="parameter"
                                        [datasets]="datasets$ | async"
                                        [parameterDescriptor]="parameterDescriptor"
                                        [formControlName]="directiveInstance || parameter.ref.id"
            ></parameter-fieldnamepattern>

            <mat-form-field *ngSwitchCase="jsonClass.FieldParameterDescriptor"
                            [id]="directiveInstance || parameter.ref.id">
                <input matInput type="text" [placeholder]="parameterDescriptor.defaultValue"
                       [formControlName]="directiveInstance || parameter.ref.id"
                       [id]="directiveInstance || parameter.ref.id">
                <mat-label>{{parameterDescriptor.info.displayName}}</mat-label>
                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear" (click)="value=''">
                    <mat-icon>close</mat-icon>
                </button>
            </mat-form-field>

            <parameter-list *ngSwitchCase="jsonClass.TextListParameterDescriptor"
                            [formControlName]="directiveInstance || parameter.ref.id"
                            [id]="directiveInstance || parameter.ref.id" [parameter]="parameter"
                            [datasets]="datasets$ | async"
                            [parameterDescriptor]="parameterDescriptor"
            >
            </parameter-list>


            <parameter-list *ngSwitchCase="jsonClass.FieldNameListParameterDescriptor"
                            [formControlName]="directiveInstance|| parameter.ref.id"
                            [id]="directiveInstance || parameter.ref.id" [parameter]="parameter"
                            [datasets]="datasets$ | async"
                            [parameterDescriptor]="parameterDescriptor"
            >
            </parameter-list>

            <parameter-map *ngSwitchCase="jsonClass.FieldListParameterDescriptor"
                           [formControlName]="directiveInstance || parameter.ref.id"
                           [parameterDescriptor]="parameterDescriptor"
                           [datasets]="datasets$ | async"
                           [id]="directiveInstance || parameter.ref.id" [parameter]="parameter"
            ></parameter-map>

            <parameter-directive *ngSwitchCase="jsonClass.FieldDirectiveSequenceParameterDescriptor"
                                 [formControlName]="directiveInstance || parameter.ref.id"
                                 [fieldDirectiveSequenceParameterDescriptor]="parameterDescriptor"
                                 [parameter]="parameter"
                                 [id]="directiveInstance || parameter.ref.id"
                                 [datasets]="datasets$ | async"
            ></parameter-directive>


            <div class="parameter-required" *ngIf="!isValid">{{parameterDescriptor.info.displayName}}
                {{'PARAMETERCOMPONENT.ISREQUIRED' | translate}}
            </div>
        </div>

    `,
    providers: []
})
export class ParameterComponent implements OnInit {
    @Input() public parameterDescriptor: ResolvedParameterDescriptor;
    @Input() public parameter: Parameter;
    @Input() public directiveInstance?: string;

    @Input() public form: FormGroup;
    datasets$: BehaviorSubject<Dataset[]> = new BehaviorSubject<Dataset[]>([]);
    public jsonClass: typeof ParameterDescriptorJsonClass = ParameterDescriptorJsonClass;

    @Input('datasets') set datasets(data: Dataset[]) {
        this.datasets$.next(data);
    };

    public parameterGroupMapping: Map<Parameter, ResolvedParameterDescriptor> = new Map();

    ngOnInit() {
        if (this.directiveInstance) {
            this.directiveInstance = this.directiveInstance + ':' + this.parameter.ref.id;
        }
        if (this.parameterDescriptor.jsonClass === ParameterDescriptorJsonClass.ParameterGroupDescriptor) {
            (this.parameterDescriptor as ParameterGroupDescriptor).parameters.forEach((descriptor, i) =>
                this.parameterGroupMapping.set((this.parameter as ParameterGroup).parameters.parameters[i], descriptor)
            )
        }

    }

    getKeys(map: Map<any, any>): any[] {
        return Array.from(map.keys());
    }

    get isValid() {
        if (this.parameterDescriptor.jsonClass === ParameterDescriptorJsonClass.ParameterGroupDescriptor) {
            let groupValids: boolean[] = (this.parameter as ParameterGroup).parameters.parameters.map(parameter =>
                this.form.controls[parameter.ref.id].valid);
            return groupValids.every((value) => value);
        }
        return this.form.controls[this.directiveInstance || this.parameter.ref.id].valid;
    }

}
