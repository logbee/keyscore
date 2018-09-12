import {Component, Input, OnInit} from "@angular/core";
import {Draggable} from "./models/contract";
import {FormGroup} from "@angular/forms";
import {ParameterControlService} from "../../../common/parameter/services/parameter-control.service";
import {BlockConfiguration} from "./models/block-configuration.model";
import {Observable} from "rxjs";

@Component({
    selector: "configurator",
    template: `
        <div fxLayout="column" fxLayoutWrap fxLayoutGap="10px" fxLayoutAlign="center">
            <div fxLayoutAlign="end" class="configurator-header">
                <button mat-raised-button color="primary">Done</button>
            </div>
            <div>{{selectedDraggable?.getDraggableModel().blockDescriptor.displayName}}</div>

            <div *ngFor="let parameter of selectedDraggable?.getDraggableModel().blockDescriptor.parameters">
                <app-parameter [parameterDescriptor]="parameter"
                               [form]="form"></app-parameter>
            </div>
        </div>
    `
})

export class ConfigurationComponent implements OnInit {
    @Input() selectedDraggable$: Observable<Draggable>;
    @Input() isOpened: boolean;
    public form: FormGroup;
    public selectedDraggable: Draggable;

    constructor(private parameterService: ParameterControlService) {

    }

    public ngOnInit(): void {
        this.selectedDraggable$.subscribe(selectedDraggable => {
            console.log("configuration on init subscribe");
            this.selectedDraggable = selectedDraggable;
            this.form = this.parameterService.toFormGroup(
                selectedDraggable.getDraggableModel().blockDescriptor.parameters,
                selectedDraggable.getDraggableModel().blockConfiguration.parameters)

        })

    }

}