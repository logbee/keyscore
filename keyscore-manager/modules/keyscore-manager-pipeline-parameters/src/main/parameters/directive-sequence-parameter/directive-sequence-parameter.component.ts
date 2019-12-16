import {Component} from "@angular/core";
import {ParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/ParameterComponent";
import {
    FieldDirectiveSequenceParameterDescriptor,
    FieldDirectiveSequenceParameter,
    DirectiveConfiguration,
    FieldDirectiveSequenceConfiguration
} from '@keyscore-manager-models/src/main/parameters/directive.model';
import uuid = require("uuid");
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";


@Component({
    selector: 'ks-parameter-directive-sequence',
    template: `
        <mat-expansion-panel>
            <mat-expansion-panel-header fxLayout="row-reverse">
                <mat-panel-title fxFlex="row" fxLayoutAlign="center center" fxLayoutGap="8px">
                    <span>{{descriptor.displayName}}</span>
                    <mat-icon fxFlexAlign="center" [matTooltip]="descriptor.description" class="info-icon">info_outlined</mat-icon>                    
                </mat-panel-title>
            </mat-expansion-panel-header>

            <div class="sequence-parameter-body" fxLayout="column" fxLayoutGap="8px">
                <div class="sequence-list" cdkDropList (cdkDropListDropped)="drop($event)" fxLayout="column"
                     fxLayoutGap="8px">
                    <ks-directive-sequence class="sequence-draggable" *ngFor="let sequence of parameter.value"
                                           [descriptor]="descriptor"
                                           [sequence]="sequence"
                                           [autoCompleteDataList]="autoCompleteDataList"
                                           (onSequenceChange)="sequenceChanged($event)"
                                           (onDelete)="deleteSequence(sequence)"
                                           cdkDrag
                    >
                    </ks-directive-sequence>
                </div>

                <ks-button-add-directive (onAdd)="addSequence()"></ks-button-add-directive>
            </div>


        </mat-expansion-panel>
    `,
    styleUrls: ['./directive-sequence-parameter.component.scss']
})
export class DirectiveSequenceParameterComponent extends ParameterComponent<FieldDirectiveSequenceParameterDescriptor, FieldDirectiveSequenceParameter> {


    constructor(private _parameterFactory: ParameterFactoryService) {
        super();
    }

    private sequenceChanged(sequence: FieldDirectiveSequenceConfiguration) {
        const index = this.parameter.value.findIndex(conf => conf.id === sequence.id);
        if (index < 0) {
            throw new Error(`[DirectiveSequenceParameter] The updated sequence: ${sequence.id} does not exist.`);
        }
        this.parameter.value.splice(index, 1, sequence);
        this.propagateChange();
    }

    private addSequence() {
        this.parameter.value.push({
            id: uuid(),
            parameters: {
                parameters: this.descriptor.parameters.map(descriptor =>
                    this._parameterFactory.parameterDescriptorToParameter(descriptor))
            },
            directives: []
        });
        this.propagateChange();
    }

    private deleteSequence(sequence: FieldDirectiveSequenceConfiguration) {
        const index = this.parameter.value.findIndex(seq => seq.id === sequence.id);
        if (index > -1) {
            this.parameter.value.splice(index, 1);
            this.propagateChange();
        }
    }

    private drop(event: CdkDragDrop<FieldDirectiveSequenceConfiguration>) {
        moveItemInArray(this.parameter.value, event.previousIndex, event.currentIndex);
        this.propagateChange();
    }

    private propagateChange() {
        this.emit(new FieldDirectiveSequenceParameter(this.parameter.ref, [...this.parameter.value]));
    }
}
