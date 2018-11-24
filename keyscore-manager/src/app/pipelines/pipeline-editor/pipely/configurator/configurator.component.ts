import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {BehaviorSubject, Subject, Subscription} from "rxjs";
import {filter} from "rxjs/operators";
import {deepcopy, zip} from "../../../../util";
import {Parameter} from "../../../../models/parameters/Parameter";
import {ResolvedParameterDescriptor} from "../../../../models/parameters/ParameterDescriptor";
import {ParameterControlService} from "../../../../common/parameter/service/parameter-control.service";
import {Configuration} from "../../../../models/common/Configuration";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {takeUntil} from "rxjs/internal/operators";
import * as _ from "lodash";
import {DatasetTableModel, DatasetTableRecordModel} from "../../../../models/dataset/DatasetTableModel";
import {Dataset} from "../../../../models/dataset/Dataset";
import {Observable} from "rxjs/internal/Observable";


@Component({
    selector: "configurator",
    template: `
        <div fxFill fxLayout="column" class="configurator-wrapper">
            <div fxLayout="row">
                <div fxFlex="95%" fxLayout="column" fxLayoutGap="15px" fxLayoutAlign="start">
                    <div
                            *ngIf="selectedBlock$.getValue().configuration.ref.uuid === 'init';else filterNameDescription">
                        <form [formGroup]="pipelineForm">
                            <mat-form-field>
                                <input matInput type="text" placeholder="Pipeline Name"
                                       formControlName="pipeline.name"
                                       id="pipeline.name">
                                <mat-label>{{'CONFIGURATOR.PIPELINE_NAME' | translate}}</mat-label>

                                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear"
                                        (click)="value=''">
                                    <mat-icon>close</mat-icon>
                                </button>
                            </mat-form-field>

                            <mat-form-field>
                                <textarea matInput type="text" placeholder="Pipeline Description"
                                          formControlName="pipeline.description"
                                          id="pipeline.description"></textarea>
                                <mat-label>{{'CONFIGURATOR.PIPELINE_DESCRIPTION' | translate}}</mat-label>

                                <button mat-button *ngIf="value" matSuffix mat-icon-button aria-label="Clear"
                                        (click)="value=''">
                                    <mat-icon>close</mat-icon>
                                </button>
                            </mat-form-field>
                        </form>
                    </div>
                    <ng-template #filterNameDescription>
                        <h3>{{selectedBlock$.getValue().descriptor.displayName}}</h3>
                        <p>{{selectedBlock$.getValue().descriptor.description}}</p>
                        <mat-divider></mat-divider>
                    </ng-template>
                </div>
                <button matTooltip="{{'CONFIGURATOR.HIDE' | translate}}" *ngIf="collapsibleButton" mat-mini-fab
                        color="primary"
                        (click)="collapse()" style="margin-right: 30px;">
                    <mat-icon>chevron_right</mat-icon>
                </button>
            </div>
            <div fxLayout="column" fxLayoutWrap fxLayoutGap="10px" fxLayoutAlign="center">
                <div class="configurator-body">
                    <form *ngIf="form" [formGroup]="form">
                        <app-parameter *ngFor="let parameter of getKeys(parameterMapping)" [parameter]="parameter"
                                       [parameterDescriptor]="parameterMapping.get(parameter)"
                                       [form]="form"
                                       [datasets]="datasets$ | async">
                        </app-parameter>
                    </form>
                </div>
            </div>
            <div *ngIf="showFooter" fxLayout="column" class="configurator-footer" fxLayoutGap="10px">
                <mat-divider></mat-divider>
                <div fxLayout="row" fxLayoutAlign="space-between">
                    <div fxLayout="row" fxLayoutGap="10px">
                        <button matTooltip="{{'PIPELY.REVERT_TOOLTIP'| translate}}" mat-raised-button (click)="revert()"
                                color="warn">
                            <mat-icon>undo</mat-icon>
                            {{'PIPELY.REVERT' | translate}}
                        </button>
                        <button mat-raised-button matTooltip="{{'PIPELY.RESET_TOOLTIP'| translate}}" (click)="reset()"
                                color="default">
                            <mat-icon>cancel</mat-icon>
                            {{'PIPELY.RESET' | translate}}
                        </button>
                    </div>
                    <button *ngIf="applyTestFlag;else apply" #save mat-raised-button color="primary"
                            matTooltip="{{'PIPELY.TEST_TOOLTIP'| translate}}"
                            (click)="saveConfiguration()">
                        <mat-icon>play_arrow</mat-icon>
                        {{'PIPELY.TEST' | translate}}
                    </button>
                    <ng-template #apply>
                        <button save mat-raised-button color="primary"
                                matTooltip=" {{'PIPELY.APPLY_TOOLTIP' | translate}}"
                                (click)="overwriteConfiguration()">
                            <mat-icon>done</mat-icon>
                            {{'PIPELY.APPLY' | translate}}
                        </button>
                    </ng-template>
                </div>
            </div>
        </div>
    `
})

export class ConfiguratorComponent implements OnInit, OnDestroy {
    @Input() showFooter: boolean;
    @Input() collapsibleButton: boolean;
    @Input() pipelineMetaData: { name: string, description: string } = {name: "", description: ""};

    @Input('selectedBlock') set selectedBlock(block: { configuration: Configuration, descriptor: BlockDescriptor }) {
        if (block.configuration && block.descriptor) {
            this.selectedBlock$.next(block);
        } else {
            this.selectedBlock$.next(this.initBlock);
        }
    }

    @Input('datasets') set datasets(data: Dataset[]) {
        this.datasets$.next(data);
    }

    @Output() closeConfigurator: EventEmitter<void> = new EventEmitter();
    @Output() onSave: EventEmitter<Configuration> = new EventEmitter();
    @Output() onRevert: EventEmitter<void> = new EventEmitter();
    @Output() onShowConfigurator: EventEmitter<boolean> = new EventEmitter();
    @Output() onSavePipelineMetaData: EventEmitter<{ name: string, description: string }> = new EventEmitter();
    @Output() onOverwriteConfiguration: EventEmitter<void> = new EventEmitter();

    private initBlock = {
        configuration: {ref: {uuid: "init"}, parent: null, parameters: []},
        descriptor: {
            ref: null,
            displayName: "",
            description: "",
            previousConnection: null,
            nextConnection: null,
            parameters: [],
            categories: []
        }
    };

    private selectedBlock$ = new BehaviorSubject<{
        configuration: Configuration,
        descriptor: BlockDescriptor
    }>(this.initBlock);

    private datasets$ = new BehaviorSubject<Dataset[]>([]);

    isVisible: boolean = true;
    applyTestFlag: boolean = true;
    isAlive: Subject<void> = new Subject();
    form: FormGroup;
    pipelineForm: FormGroup;
    parameterMapping: Map<Parameter, ResolvedParameterDescriptor> = new Map();

    private lastID: string = "";
    private lastValues = null;

    constructor(private parameterService: ParameterControlService) {
    }

    public ngOnInit(): void {
        this.selectedBlock$.pipe(takeUntil(this.isAlive), filter(block => block.configuration.ref.uuid !== this.lastID)).subscribe(selectedBlock => {
            this.lastID = selectedBlock.configuration.ref.uuid;

            this.parameterMapping =
                new Map(zip([selectedBlock.configuration.parameters,
                    selectedBlock.descriptor.parameters
                ]));
            if (this.form) {
                this.form.reset();
            }
            this.form = this.parameterService.toFormGroup(this.parameterMapping);

            this.form.valueChanges.subscribe(values => {
                console.log(values);
                if (!this.isAllNullOrEmpty(values) && !this.showFooter && !_.isEqual(this.lastValues, values)) {
                    this.lastValues = values;
                    this.saveConfiguration();
                }
            });
        });

        this.pipelineForm = new FormGroup({
            'pipeline.name': new FormControl(this.pipelineMetaData.name),
            'pipeline.description': new FormControl(this.pipelineMetaData.description)
        });

        this.pipelineForm.valueChanges.subscribe(val => {
            this.onSavePipelineMetaData.emit({name: val['pipeline.name'], description: val['pipeline.description']});
        });
    }

    private isAllNullOrEmpty(obj: Object): boolean {
        const values = Object.values(obj);
        for (let prop of values) {
            if (prop) return false;
        }
        return true;
    }

    reset() {
        this.selectedBlock$.getValue().configuration.parameters.forEach(parameter => {
                this.form.controls[parameter.ref.id].setValue(parameter.value);
            }
        );
        this.closeConfigurator.emit();
    }

    collapse() {
        this.isVisible = !this.isVisible;
        console.log("Current Block:", this.selectedBlock);
        this.onShowConfigurator.emit(this.isVisible);
    }

    revert() {
        this.reset();
        this.onRevert.emit()
    }

    saveConfiguration() {
        let configuration: Configuration = deepcopy(this.selectedBlock$.getValue().configuration);
        if (configuration.ref.uuid !== 'init') {
            configuration.parameters.forEach((parameter) => {
                if (this.form.controls[parameter.ref.id]) {
                    parameter.value = this.form.controls[parameter.ref.id].value;
                }
            });
            this.onSave.emit(configuration);
        }
        this.applyTestFlag = false;
    }

    overwriteConfiguration() {
        this.applyTestFlag = true;
        this.onOverwriteConfiguration.emit();
    }

    getKeys(map: Map<any, any>): any[] {
        return Array.from(map.keys());
    }

    ngOnDestroy(): void {
        this.isAlive.next();
    }
}