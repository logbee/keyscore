import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild} from "@angular/core";
import {Draggable} from "./models/contract";
import {FormGroup} from "@angular/forms";
import {BlockConfiguration} from "./models/block-configuration.model";
import {BehaviorSubject, fromEvent, Observable, Subject} from "rxjs";
import {distinctUntilChanged} from "rxjs/operators";
import {deepcopy, zip} from "../../../util";
import {Parameter} from "../../../models/parameters/Parameter";
import {ResolvedParameterDescriptor} from "../../../models/parameters/ParameterDescriptor";
import {Configuration} from "../../../models/common/Configuration";

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
                    <button #save mat-raised-button color="primary" (click)="saveSource$.next()">
                        <mat-icon>save</mat-icon>
                        Save
                    </button>
                </div>
            </div>
            <mat-divider></mat-divider>
            <div fxLayoutAlign="start">{{selectedDraggable?.getDraggableModel().blockDescriptor.displayName}}</div>

            <configuration [parametersMapping$]="parametersMapping$" [saveConfiguration$]="save$" (updateConfiguration)="onUpdateConfiguration($event)">
            </configuration>
        </div>
    `
})

export class ConfiguratorComponent implements OnInit {
    @Input() selectedDraggable$: Observable<Draggable>;
    @Input() isOpened: boolean;
    @Output() closeConfigurator: EventEmitter<void> = new EventEmitter();
    public selectedDraggable: Draggable;

    saveSource$:Subject<void> = new Subject();
    save$: Observable<void> = this.saveSource$.asObservable();

    parametersMappingSource$: BehaviorSubject<Map<Parameter, ResolvedParameterDescriptor>> = new BehaviorSubject(new Map());
    parametersMapping$: Observable<Map<Parameter, ResolvedParameterDescriptor>> = this.parametersMappingSource$.asObservable();

    constructor() {

    }

    public ngOnInit(): void {
        this.selectedDraggable$.pipe(distinctUntilChanged()).subscribe(selectedDraggable => {
            console.log("configuration on init subscribe");
            console.log(selectedDraggable.getDraggableModel().blockConfiguration.parameters);
            this.selectedDraggable = selectedDraggable;
            this.parametersMappingSource$.next(
                new Map(zip([selectedDraggable.getDraggableModel().blockConfiguration.parameters,
                    selectedDraggable.getDraggableModel().blockDescriptor.parameters])));
        });

    }

    /*cancel() {
        this.selectedDraggable.getDraggableModel().blockConfiguration.parameters.forEach(parameter =>
            this.form.controls[parameter.ref.uuid].setValue(parameter.value)
        );
        this.closeConfigurator.emit();

    }*/

    onUpdateConfiguration(configuration:Configuration) {
        let blockConfiguration = deepcopy(this.selectedDraggable.getDraggableModel().blockConfiguration);
        blockConfiguration.parameters = configuration.parameters;
        this.selectedDraggable.getDraggableModel().blockConfiguration = blockConfiguration;
        console.log(this.selectedDraggable.getDraggableModel().blockConfiguration.parameters);
    }

}