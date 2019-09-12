import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {BehaviorSubject, Subject} from "rxjs";
import {filter} from "rxjs/operators";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {takeUntil} from "rxjs/internal/operators";
import * as _ from "lodash";
import {
    Parameter,
    ParameterDescriptor,
    ParameterMap
} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter.model";
import {Configuration} from "@/../modules/keyscore-manager-models/src/main/common/Configuration";
import {Dataset} from "@/../modules/keyscore-manager-models/src/main/dataset/Dataset";


@Component({
    selector: "configurator",
    template: `
        <div fxFill fxLayout="column" class="configurator-wrapper mat-elevation-z8">
            <div fxLayout="row">
                <div *ngIf="!(config$|async).conf">
                    <div fxFlex="95%" fxLayout="column" fxLayoutGap="15px" fxLayoutAlign="start">
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
                </div>
                <div *ngIf="(config$|async) as config">
                    <div fxFlex="95%" fxLayout="column" fxLayoutGap="15px" fxLayoutAlign="start">
                        <h3>
                            <p style="margin-bottom: 5px">{{config?.descriptor?.displayName}}</p>
                            <p style="margin-bottom: 0; font-family: monospace;font-size: small">
                                {{config?.uuid}}</p>
                        </h3>
                        <p>{{config?.descriptor?.description}}</p>
                        <mat-divider></mat-divider>
                        <div class="configurator-body">
                            <parameter-form [parameters]="parameterMap$|async"
                                            [autoCompleteDataList]="autoCompleteOptions"
                                            (onValueChange)="saveConfiguration($event)"></parameter-form>
                        </div>
                    </div>
                </div>
                <button matTooltip="{{'CONFIGURATOR.HIDE' | translate}}" *ngIf="collapsibleButton" mat-mini-fab
                        color="primary"
                        (click)="collapse()" style="margin-right: 30px;">
                    <mat-icon>chevron_right</mat-icon>
                </button>
            </div>
        </div>
    `,
    styleUrls: ['./configurator.component.scss']
})

export class ConfiguratorComponent implements OnInit, OnDestroy {
    @Input() collapsibleButton: boolean;
    @Input() pipelineMetaData: { name: string, description: string } = {name: "", description: ""};

    @Input('config') set config(val: { conf: Configuration, descriptor: BlockDescriptor, uuid: string }) {
        if (val) {
            this._config = val.conf;
            this.config$.next(val);
        } else {
            this.config$.next(undefined);
            this._config = undefined;
        }
    }

    @Input('datasets') set datasets(data: Dataset[]) {
        this.autoCompleteOptions = Array.from(
            new Set([].concat.apply(data.map(data =>
                data.records.map(rec =>
                    rec.fields.map(field => field.name))))));
    }

    private autoCompleteOptions: string[];

    @Output() closeConfigurator: EventEmitter<void> = new EventEmitter();
    @Output() onSave: EventEmitter<Configuration> = new EventEmitter();
    @Output() onRevert: EventEmitter<void> = new EventEmitter();
    @Output() onShowConfigurator: EventEmitter<boolean> = new EventEmitter();
    @Output() onSavePipelineMetaData: EventEmitter<{ name: string, description: string }> = new EventEmitter();
    @Output() onOverwriteConfiguration: EventEmitter<void> = new EventEmitter();


    private config$ = new BehaviorSubject<{ conf: Configuration, descriptor: BlockDescriptor }>(undefined);
    private _config: Configuration;

    isVisible: boolean = true;
    form: FormGroup;
    pipelineForm: FormGroup;
    parameterMap$: Subject<ParameterMap> = new Subject();

    unsubscribe$: Subject<void> = new Subject();

    private lastID: string = "";


    constructor() {
    }

    public ngOnInit(): void {

        this.config$.pipe(takeUntil(this.unsubscribe$)).subscribe(config => {
            //this.lastID = config.conf.ref.uuid;
            this.createParameterMap(config);
        });


        this.pipelineForm = new FormGroup({
            'pipeline.name': new FormControl(this.pipelineMetaData.name),
            'pipeline.description': new FormControl(this.pipelineMetaData.description)
        });

        this.pipelineForm.valueChanges.subscribe(val => {
            this.onSavePipelineMetaData.emit({name: val['pipeline.name'], description: val['pipeline.description']});
        });
    }

    private createParameterMap(config: { conf: Configuration, descriptor: BlockDescriptor }) {
        console.log("CREATE PARAMETER MAP");
        if (!config.conf) {
            this.parameterMap$.next(null);
            console.log("CREATE PARAMETER MAP NULL");
            return;
        }
        const parameterTuples: [Parameter, ParameterDescriptor][] =
            _.zip(config.conf.parameterSet.parameters, config.descriptor.parameters);
        let parameterMap: ParameterMap = {parameters: {}};
        parameterTuples.forEach(([parameter, descriptor]) =>
            parameterMap.parameters[descriptor.ref.id] = [parameter, descriptor]);
        this.parameterMap$.next(parameterMap);
        console.log("CREATED PARAMETERMAP", parameterMap);
    }

    reset() {
        this.config$.getValue().conf.parameterSet.parameters.forEach(parameter => {
                this.form.controls[parameter.ref.id].setValue(parameter.value);
            }
        );
        this.closeConfigurator.emit();
    }

    collapse() {
        this.isVisible = !this.isVisible;
        this.onShowConfigurator.emit(this.isVisible);
    }


    saveConfiguration(parameter: Parameter) {
        let configuration: Configuration = _.cloneDeep(this._config);
        let paramIndex = configuration.parameterSet.parameters.findIndex(param => param.ref.id === parameter.ref.id);
        if (paramIndex > -1) {
            configuration.parameterSet.parameters.splice(paramIndex, 1, parameter)
            this.onSave.emit(configuration);
        }

    }

    ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }
}