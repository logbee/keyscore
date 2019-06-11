import {Component, forwardRef, Input, OnInit} from "@angular/core";
import {ControlValueAccessor, FormGroup, NG_VALUE_ACCESSOR} from "@angular/forms";
import {
    Dataset,
    DirectiveConfiguration,
    FieldDirectiveSequenceConfiguration,
    FieldDirectiveSequenceParameterDescriptor,
    FieldNameHint,
    generateRef,
    Parameter,
    ParameterJsonClass,
    ResolvedFieldDirectiveDescriptor,
    ResolvedParameterDescriptor
} from "keyscore-manager-models";
import {BehaviorSubject} from "rxjs/index";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {ParameterControlService} from "./service/parameter-control.service";
import {ParameterFactoryService} from "./service/parameter-factory.service";
import * as _ from 'lodash'
import uuid = require("uuid");

@Component({
    selector: "parameter-directive",
    template:
            `
        <div fxLayout="row" fxLayoutAlign="space-between center">
            <div>Directives</div>
            <button mat-icon-button color="accent" (click)="addDirectiveSequence()">
                <mat-icon>add_circle_outline</mat-icon>
            </button>
        </div>
        <div *ngIf="parameterValues.length" cdkDropList class="field-directive-sequence"
             (cdkDropListDropped)="dropFieldDirectiveSequence($event)">
            <mat-expansion-panel class="field-directives-box"
                                 *ngFor="let fieldDirectiveSequence of parameterValues;index as sequenceIndex" cdkDrag
                                 [expanded]="isSequenceExpanded[sequenceIndex]" [hideToggle]>
                <mat-expansion-panel-header [class.is-active]="isSequenceExpanded[sequenceIndex]"
                                            (click)="expandSequence(sequenceIndex)" [collapsedHeight]="'*'"
                                            [expandedHeight]="'*'">
                    <div fxLayout="row" fxLayoutAlign="space-between center" class="sequence-header">

                        <form *ngIf="fieldDirectiveSequence.parameters.parameters.length"
                              [formGroup]="sequenceFormGroups.get(fieldDirectiveSequence.id)"
                              class="sequence-form"
                              propagationStop>
                            <app-parameter
                                    *ngFor="let parameter of getKeys(sequenceParameterMappings.get(fieldDirectiveSequence.id))"
                                    [parameter]="parameter"
                                    [parameterDescriptor]="sequenceParameterMappings.get(fieldDirectiveSequence.id).get(parameter)"
                                    [form]=sequenceFormGroups.get(fieldDirectiveSequence.id)
                                    [datasets]="datasets$|async"
                            ></app-parameter>
                        </form>
                        <button propagationStop mat-icon-button color="warn"
                                (click)="removeDirectiveSequence(sequenceIndex)">
                            <mat-icon matTooltip="Remove all directives for this field.">close
                            </mat-icon>
                        </button>
                    </div>
                </mat-expansion-panel-header>
                <div *ngIf="fieldDirectiveSequence.directives.length" cdkDropList class="field-directive-sequence"
                     (cdkDropListDropped)="dropFieldDirective($event,sequenceIndex)" fxLayout="column">
                    <div class="field-directives-box"
                         *ngFor="let fieldDirective of fieldDirectiveSequence.directives;index as directiveIndex"
                         cdkDrag
                         [class.directive]="getKeys(directiveParameterMappings.get(fieldDirective.instance.uuid)).length">
                        <div fxLayout="row" fxLayoutAlign="space-between center" class="directive-header">

                            <div class="margin-left-5">
                                {{getFieldDirectiveDescriptor(fieldDirective).info.displayName}}
                            </div>
                            <button mat-icon-button color="warn"
                                    (click)="removeDirective(sequenceIndex,directiveIndex)">
                                <mat-icon matTooltip="Remove this directive.">close</mat-icon>
                            </button>
                        </div>

                        <form *ngIf="getKeys(directiveParameterMappings.get(fieldDirective.instance.uuid)).length"
                              [formGroup]="directiveFormGroups.get(fieldDirective.instance.uuid)"
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
                <button mat-button class="directive-add " fxLayout="row" fxLayoutAlign="center center"
                        fxFlexAlign="stretch" [matMenuTriggerFor]="directiveMenu">
                    <mat-icon matTooltip="Add a new directive to this field" color="accent">add_circle_outline
                    </mat-icon>
                </button>
                <mat-menu #directiveMenu>
                    <button fxLayout="row" fxLayoutAlign="space-between center" mat-menu-item
                            *ngFor="let directiveDescriptor of fieldDirectiveSequenceParameterDescriptor.directives"
                            (click)="addDirective(fieldDirectiveSequence,directiveDescriptor,sequenceIndex)">
                        <mat-icon>add_circle_outline</mat-icon>
                        {{directiveDescriptor.info.displayName}}
                    </button>
                </mat-menu>
            </mat-expansion-panel>
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
    @Input() public fieldDirectiveSequenceParameterDescriptor: FieldDirectiveSequenceParameterDescriptor;

    @Input('datasets') set datasets(data: Dataset[]) {
        this.datasets$.next(data);
    }

    private datasets$: BehaviorSubject<Dataset[]> = new BehaviorSubject<Dataset[]>([]);

    public fieldNameHint: typeof FieldNameHint = FieldNameHint;

    public parameterValues: FieldDirectiveSequenceConfiguration[] = [];
    public isSequenceExpanded: boolean[] = [];

    public sequenceFormGroups: Map<string, FormGroup> = new Map();
    public directiveFormGroups: Map<string, FormGroup> = new Map();
    public directiveParameterMappings: Map<string, Map<Parameter, ResolvedParameterDescriptor>> = new Map();
    public sequenceParameterMappings: Map<string, Map<Parameter, ResolvedParameterDescriptor>> = new Map();

    public constructor(private parameterService: ParameterControlService, private parameterFactory: ParameterFactoryService) {

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
            this.parameterValues.map((sequence, seqIndex) =>
                sequence.directives.forEach((directive, directiveIndex) =>
                    this.createDirectiveSubForm(this.parameterValues, seqIndex, directiveIndex,
                        this.fieldDirectiveSequenceParameterDescriptor.directives.find(dir =>
                            dir.ref.uuid === directive.ref.uuid), sequence)))

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

    public addDirectiveSequence() {
        console.log("FieldDirectiveSequenceConf after init: ");
        let newValues = [...this.parameterValues];

        let fieldDirectiveSequence: FieldDirectiveSequenceConfiguration = {
            id: uuid(),
            parameters: {
                jsonClass: ParameterJsonClass.ParameterSet,
                parameters: this.fieldDirectiveSequenceParameterDescriptor.parameters.map(parameter =>
                    this.parameterFactory.parameterDescriptorToParameter(parameter))
            },
            directives: [],

        };
        console.log("FieldDirectiveSequenceConf after init: ", fieldDirectiveSequence);
        newValues.push(fieldDirectiveSequence);
        this.createSequenceForm(fieldDirectiveSequence, newValues.length - 1);
        this.isSequenceExpanded.push(true);
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
                    this.parameterFactory.parameterDescriptorToParameter(parameter))
            }
        });
        const index = newValues[seqIndex].directives.length - 1;
        this.createDirectiveSubForm(newValues, seqIndex, index, directive, sequence);
        this.isSequenceExpanded[seqIndex] = true;
        this.writeValue(newValues);
    }

    public expandSequence(seqIndex: number) {
        this.isSequenceExpanded[seqIndex] = !this.isSequenceExpanded[seqIndex];
    }

    private createSequenceForm(sequence: FieldDirectiveSequenceConfiguration, seqIndex: number) {
        let parameterMapping: Map<Parameter, ResolvedParameterDescriptor> =
            new Map(_.zip(sequence.parameters.parameters, this.fieldDirectiveSequenceParameterDescriptor.parameters));
        let form = this.parameterService.toFormGroup(parameterMapping);
        this.sequenceParameterMappings.set(sequence.id, parameterMapping);
        form.valueChanges.subscribe(values => {
            let newValues = [...this.parameterValues];
            newValues[seqIndex].parameters.parameters.forEach(parameter =>
                parameter.value = values[parameter.ref.id]);
            this.writeValue(newValues);
        });
        this.sequenceFormGroups.set(sequence.id, form);
    }

    private createDirectiveSubForm(values, currentSeqIndex, index, directive: ResolvedFieldDirectiveDescriptor,
                                   sequence: FieldDirectiveSequenceConfiguration) {
        let parameterMapping: Map<Parameter, ResolvedParameterDescriptor> =
            new Map(_.zip(values[currentSeqIndex].directives[index].parameters.parameters, directive.parameters));
        this.directiveParameterMappings.set(sequence.directives[index].instance.uuid, parameterMapping);
        let form = this.parameterService.toFormGroup(parameterMapping, sequence.directives[index].instance.uuid);
        let directiveConfiguration = this.parameterValues[currentSeqIndex].directives[index];
        form.valueChanges.subscribe(values => {
            let instanceRef = Object.keys(values)[0].substring(0, 36);
            let newValues = [...this.parameterValues];
            let currentSeqIndex = newValues.findIndex(seq => seq.id === sequence.id);
            let index = newValues[currentSeqIndex].directives.findIndex(dir => dir.instance.uuid === instanceRef);
            newValues[currentSeqIndex].directives[index].parameters.parameters.forEach(parameter =>
                parameter.value = values[instanceRef + ':' + parameter.ref.id]);
            this.writeValue(newValues);

        });
        this.directiveFormGroups.set(sequence.directives[index].instance.uuid, form);
    }

    public removeDirectiveSequence(index: number) {
        const newValues = [...this.parameterValues];
        newValues.splice(index, 1);
        this.isSequenceExpanded.splice(index, 1);
        this.writeValue(newValues);
    }

    public dropFieldDirectiveSequence(event: CdkDragDrop<FieldDirectiveSequenceConfiguration[]>) {
        let newValues = [...this.parameterValues];
        moveItemInArray(newValues, event.previousIndex, event.currentIndex);
        moveItemInArray(this.isSequenceExpanded, event.previousIndex, event.currentIndex);
        this.writeValue(newValues);

    }

    public dropFieldDirective(event: CdkDragDrop<DirectiveConfiguration[]>, sequenceIndex: number) {
        let newValues = [...this.parameterValues];
        moveItemInArray(newValues[sequenceIndex].directives, event.previousIndex, event.currentIndex);
        this.writeValue(newValues);
    }

    public getFieldDirectiveDescriptor(directive: DirectiveConfiguration): ResolvedFieldDirectiveDescriptor {
        return this.fieldDirectiveSequenceParameterDescriptor.directives.find(dir => dir.ref.uuid === directive.ref.uuid);
    }

    getKeys(map: Map<any, any>): any[] {
        return Array.from(map.keys());
    }
}
