import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {BehaviorSubject, Subject} from "rxjs";
import {distinctUntilChanged} from "rxjs/operators";
import {deepcopy, zip} from "../../../util";
import {Parameter} from "../../../models/parameters/Parameter";
import {ResolvedParameterDescriptor} from "../../../models/parameters/ParameterDescriptor";
import {ParameterControlService} from "../../../common/parameter/service/parameter-control.service";
import {Configuration} from "../../../models/common/Configuration";
import {BlockDescriptor} from "./models/block-descriptor.model";
import {takeUntil} from "rxjs/internal/operators";

@Component({
    selector: "configurator",
    template: `
        <div fxFill fxLayout="column" class="configurator-wrapper">
            <div fxLayout="column" fxLayoutGap="15px" fxLayoutAlign="start">
                <h3>{{selectedBlock$.getValue().descriptor.displayName}}</h3>
                <p>{{selectedBlock$.getValue().descriptor.description}}</p>
                <mat-divider></mat-divider>
            </div>
            <div fxLayout="column" fxLayoutWrap fxLayoutGap="10px" fxLayoutAlign="center">
                <div class="configurator-body">
                    <div *ngIf="form" [formGroup]="form">
                        <app-parameter *ngFor="let parameter of getKeys(parameterMapping)" [parameter]="parameter"
                                       [parameterDescriptor]="parameterMapping.get(parameter)"
                                       [form]="form"></app-parameter>
                    </div>
                </div>
            </div>
            <div *ngIf="showFooter" fxLayout="column" class="configurator-footer" fxLayoutGap="10px">
                <mat-divider></mat-divider>
                <div fxLayout="row" fxLayoutAlign="space-between">
                    <div fxLayout="row" fxLayoutGap="10px">
                        <button matTooltip="{{'PIPELY.REVERT_TOOLTIP'| translate}}" mat-raised-button (click)="revert()" color="warn">
                            <mat-icon>undo</mat-icon>
                            {{'PIPELY.REVERT'| translate}}
                        </button>
                        <button mat-raised-button matTooltip="{{'PIPELY.RESET_TOOLTIP'| translate}}" (click)="cancel()" color="default">
                            <mat-icon>cancel</mat-icon>
                            {{'PIPELY.RESET'| translate}}
                        </button>
                    </div>
                    <button #save mat-raised-button color="primary" matTooltip="{{'PIPELY.TEST_TOOLTIP'| translate}}" (click)="saveConfiguration()">
                        <mat-icon>play_arrow</mat-icon>
                        {{'PIPELY.TEST'| translate}}
                    </button>
                </div>
            </div>
        </div>
    `
})

export class ConfiguratorComponent implements OnInit, OnDestroy {
    @Input() public showFooter: boolean;
    @Input('selectedBlock') set selectedBlock(block: { configuration: Configuration, descriptor: BlockDescriptor }) {
        if (block.configuration && block.descriptor) {
            console.log("set new block" + block.descriptor.displayName);
            this.selectedBlock$.next(block);
        }
    }

    private selectedBlock$ = new BehaviorSubject<{ configuration: Configuration, descriptor: BlockDescriptor}>(
        {
            configuration: {ref: null, parent: null, parameters: []},
            descriptor: {
                ref: null,
                displayName: "",
                description: "",
                previousConnection: null,
                nextConnection: null,
                parameters: [],
                categories: []
            }
        }
    );

    @Input() isOpened: boolean;
    @Output() closeConfigurator: EventEmitter<void> = new EventEmitter();
    @Output() onSave: EventEmitter<Configuration> = new EventEmitter();
    @Output() onRevert: EventEmitter<void> = new EventEmitter();
    isAlive: Subject<void> = new Subject();
    form: FormGroup;

    parameterMapping: Map<Parameter, ResolvedParameterDescriptor> = new Map();

    constructor(private parameterService: ParameterControlService) {

    }

    public ngOnInit(): void {
        this.selectedBlock$.pipe(takeUntil(this.isAlive), distinctUntilChanged()).subscribe(selectedBlock => {
            console.log("triggered selectedBlockInput:", selectedBlock);
            this.parameterMapping =
                new Map(zip([selectedBlock.configuration.parameters,
                    selectedBlock.descriptor.parameters
                ]));
            if (this.form) {
                this.form.reset();
            }
            this.form = this.parameterService.toFormGroup(this.parameterMapping);
        });
    }


    cancel() {
        this.selectedBlock$.getValue().configuration.parameters.forEach(parameter =>
            this.form.controls[parameter.ref.id].setValue(parameter.value)
        );
        this.closeConfigurator.emit();
    }

    revert() {
        this.onRevert.emit()
    }

    saveConfiguration() {
        let configuration: Configuration = deepcopy(this.selectedBlock$.getValue().configuration);
        configuration.parameters.forEach((parameter) => {
            parameter.value = this.form.controls[parameter.ref.id].value;
        });
        this.onSave.emit(configuration);
    }

    getKeys(map: Map<any, any>): any[] {
        return Array.from(map.keys());
    }

    ngOnDestroy(): void {
        this.isAlive.next();
    }
}