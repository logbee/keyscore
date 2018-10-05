import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Draggable} from "./models/contract";
import {FormGroup} from "@angular/forms";
import {Observable} from "rxjs";
import {distinctUntilChanged} from "rxjs/operators";
import {deepcopy, zip} from "../../../util";
import {Parameter} from "../../../models/parameters/Parameter";
import {ResolvedParameterDescriptor} from "../../../models/parameters/ParameterDescriptor";
import {ParameterControlService} from "../../../common/parameter/service/parameter-control.service";
import {BlockConfiguration} from "./models/block-configuration.model";

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
                    <button #save mat-raised-button color="primary" (click)="saveConfiguration()">
                        <mat-icon>save</mat-icon>
                        Save
                    </button>
                </div>
            </div>
            <mat-divider></mat-divider>
            <div class="configurator-body">
                <div fxLayoutAlign="start">{{selectedDraggable?.getDraggableModel().blockDescriptor.displayName}}</div>

                <div *ngIf="form" [formGroup]="form">
                    <app-parameter *ngFor="let parameter of getKeys(parameterMapping)" [parameter]="parameter"
                                   [parameterDescriptor]="parameterMapping.get(parameter)"
                                   [form]="form"></app-parameter>
                </div>
            </div>

        </div>
    `
})

export class ConfiguratorComponent implements OnInit {
    @Input() selectedDraggable$: Observable<Draggable>;
    @Input() isOpened: boolean;
    @Output() closeConfigurator: EventEmitter<void> = new EventEmitter();
    public selectedDraggable: Draggable;

    form: FormGroup;

    parameterMapping: Map<Parameter, ResolvedParameterDescriptor> = new Map();

    constructor(private parameterService: ParameterControlService) {

    }

    public ngOnInit(): void {

        this.selectedDraggable$.pipe(distinctUntilChanged()).subscribe(selectedDraggable => {
            console.log("configuration on init subscribe");
            console.log(selectedDraggable.getDraggableModel().blockConfiguration.parameters);
            this.selectedDraggable = selectedDraggable;
            this.parameterMapping =
                new Map(zip([selectedDraggable.getDraggableModel().blockConfiguration.parameters,
                    selectedDraggable.getDraggableModel().blockDescriptor.parameters
                ]));
            console.log("parameterMAppoin:  ",this.parameterMapping);
            if (this.form) {
                this.form.reset();
            }
            this.form = this.parameterService.toFormGroup(this.parameterMapping);
            console.log("FORMCONTROLS: ",this.form.controls);
        });

    }

    cancel() {
        this.selectedDraggable.getDraggableModel().blockConfiguration.parameters.forEach(parameter =>
            this.form.controls[parameter.ref.id].setValue(parameter.value)
        );
        this.closeConfigurator.emit();

    }

    saveConfiguration() {
        let blockConfiguration: BlockConfiguration = deepcopy(this.selectedDraggable.getDraggableModel().blockConfiguration);
        blockConfiguration.parameters.forEach((parameter) => {
            console.log("CONTROLS: ", this.form.controls);
            console.log("PARAMETERREF:", parameter.ref);
            parameter.value = this.form.controls[parameter.ref.id].value;
        });
        this.selectedDraggable.getDraggableModel().blockConfiguration = blockConfiguration;
        console.log(this.selectedDraggable.getDraggableModel().blockConfiguration.parameters);
    }

    getKeys(map: Map<any, any>): any[] {
        return Array.from(map.keys());
    }

}