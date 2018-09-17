import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Draggable} from "./models/contract";
import {FormGroup} from "@angular/forms";
import {ParameterControlService} from "../../../common/parameter/services/parameter-control.service";
import {BlockConfiguration} from "./models/block-configuration.model";
import {Observable} from "rxjs";
import {distinctUntilChanged} from "rxjs/operators";
import {deepcopy, zip} from "../../../util";

@Component({
    selector: "configurator",
    template: `
        <div fxLayout="column" fxLayoutWrap fxLayoutGap="10px" fxLayoutAlign="center">
            <div fxLayout="row" fxLayoutAlign="space-between" class="configurator-header">
                <button mat-raised-button (click)="cancel()" color="default">
                    <mat-icon>cancel</mat-icon>
                    Cancel
                </button>
                <div>
                    <button mat-raised-button color="primary" (click)="saveConfiguration()">
                        <mat-icon>save</mat-icon>
                        Save
                    </button>
                </div>
            </div>
            <mat-divider></mat-divider>
            <div fxLayoutAlign="start">{{selectedDraggable?.getDraggableModel().blockDescriptor.displayName}}</div>

            <div *ngFor="let parameters of zippedParameters">
                <app-parameter [parameterDescriptor]="parameters[1]" [parameter]="parameters[0]"
                               [form]="form"></app-parameter>
            </div>
        </div>
    `
})

export class ConfigurationComponent implements OnInit {
    @Input() selectedDraggable$: Observable<Draggable>;
    @Input() isOpened: boolean;
    @Output() closeConfigurator:EventEmitter<void> = new EventEmitter();
    public form: FormGroup;
    public selectedDraggable: Draggable;
    public zippedParameters;

    constructor(private parameterService: ParameterControlService){

    }

    public ngOnInit(): void {
        this.selectedDraggable$.pipe(distinctUntilChanged()).subscribe(selectedDraggable => {
            console.log("configuration on init subscribe");
            console.log(selectedDraggable.getDraggableModel().blockConfiguration.parameters);
            this.selectedDraggable = selectedDraggable;
            this.form = this.parameterService.toFormGroup(
                selectedDraggable.getDraggableModel().blockDescriptor.parameters,
                selectedDraggable.getDraggableModel().blockConfiguration.parameters);
            this.zippedParameters = zip(
                [selectedDraggable.getDraggableModel().blockConfiguration.parameters,
                    selectedDraggable.getDraggableModel().blockDescriptor.parameters]
            );

        })

    }

    cancel() {
        this.selectedDraggable.getDraggableModel().blockConfiguration.parameters.forEach(parameter =>
            this.form.controls[parameter.ref.uuid].setValue(parameter.value)
        );
        this.closeConfigurator.emit();

    }

    saveConfiguration() {
        let blockConfiguration = deepcopy(this.selectedDraggable.getDraggableModel().blockConfiguration);
        blockConfiguration.parameters.forEach(parameter => {
            parameter.value = this.form.controls[parameter.ref.uuid].value;
        });
        this.selectedDraggable.getDraggableModel().blockConfiguration = blockConfiguration;
        console.log(this.selectedDraggable.getDraggableModel().blockConfiguration.parameters);
    }

}