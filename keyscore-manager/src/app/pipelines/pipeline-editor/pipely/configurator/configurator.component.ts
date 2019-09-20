import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {BehaviorSubject, Subject} from "rxjs";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {takeUntil} from "rxjs/internal/operators";
import * as _ from "lodash";
import {
    Parameter,
    ParameterDescriptor,
    ParameterMap
} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";
import {Dataset} from "@keyscore-manager-models/src/main/dataset/Dataset";


@Component({
    selector: "configurator",
    template: `
        <div fxFill fxLayout="column" class="configurator-wrapper mat-elevation-z8">
                <div *ngIf="!(config$|async).conf" fxFlex>
                    <div fxLayout="column" fxLayoutGap="15px" fxLayoutAlign="start">
                        <form [formGroup]="pipelineForm">
                            <mat-form-field>
                                <input #pipelineName matInput type="text" placeholder="Pipeline Name"
                                       formControlName="pipeline.name"
                                       id="pipeline.name">
                                <mat-label>{{'CONFIGURATOR.PIPELINE_NAME' | translate}}</mat-label>

                            </mat-form-field>

                            <mat-form-field>
                                <textarea #pipelineDescription matInput type="text" placeholder="Pipeline Description"
                                          formControlName="pipeline.description"
                                          id="pipeline.description"></textarea>
                                <mat-label>{{'CONFIGURATOR.PIPELINE_DESCRIPTION' | translate}}</mat-label>
                                
                            </mat-form-field>
                        </form>
                    </div>
                </div>
                <div *ngIf="(config$|async) as config" fxFlex>
                    <ng-container *ngIf="config.conf">
                        <div fxLayout="column" fxLayoutGap="15px" fxLayoutAlign="start">
                            <div>
                                <h3 style="margin-bottom: 5px">{{config?.descriptor?.displayName}}</h3>
                                <p style="margin-bottom: 0; font-family: monospace;font-size: small">
                                    {{config?.uuid}}</p>
                            </div>
                            <p>{{config?.descriptor?.description}}</p>
                            <mat-divider></mat-divider>
                            <div class="configurator-body">
                                <parameter-form [parameters]="parameterMap$|async"
                                                [autoCompleteDataList]="autoCompleteOptions"
                                                (onValueChange)="saveConfiguration($event)"></parameter-form>
                            </div>
                        </div>
                    </ng-container>
                </div>
        </div>
    `,
    styleUrls: ['./configurator.component.scss']
})

export class ConfiguratorComponent implements OnInit, OnDestroy {
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

    private autoCompleteOptions: string[] = [];

    @Output() closeConfigurator: EventEmitter<void> = new EventEmitter();
    @Output() onSave: EventEmitter<Configuration> = new EventEmitter();
    @Output() onRevert: EventEmitter<void> = new EventEmitter();
    @Output() onSavePipelineMetaData: EventEmitter<{ name: string, description: string }> = new EventEmitter();
    @Output() onOverwriteConfiguration: EventEmitter<void> = new EventEmitter();


    private config$ = new BehaviorSubject<{ conf: Configuration, descriptor: BlockDescriptor,uuid:string }>(undefined);
    private _config: Configuration;

    form: FormGroup;
    pipelineForm: FormGroup;
    parameterMap$: BehaviorSubject<ParameterMap> = new BehaviorSubject<ParameterMap>(null);

    unsubscribe$: Subject<void> = new Subject();

    constructor() {
    }

    public ngOnInit(): void {

        this.config$.pipe(takeUntil(this.unsubscribe$)).subscribe(config => {
            this.createParameterMap(config);
        });


        this.pipelineForm = new FormGroup({
            'pipeline.name': new FormControl(this.pipelineMetaData.name),
            'pipeline.description': new FormControl(this.pipelineMetaData.description)
        });

        this.pipelineForm.valueChanges.pipe(takeUntil(this.unsubscribe$)).subscribe(val => {
            this.onSavePipelineMetaData.emit({name: val['pipeline.name'], description: val['pipeline.description']});
        });

    }

    private createParameterMap(config: { conf: Configuration, descriptor: BlockDescriptor }) {
        if (!config.conf) {
            this.parameterMap$.next(null);
            return;
        }
        const parameterTuples: [Parameter, ParameterDescriptor][] =
            _.zip(config.conf.parameterSet.parameters, config.descriptor.parameters);
        let parameterMap: ParameterMap = {parameters: {}};
        parameterTuples.forEach(([parameter, descriptor]) =>
            parameterMap.parameters[descriptor.ref.id] = [parameter, descriptor]);
        this.parameterMap$.next(parameterMap);
    }


    saveConfiguration(parameter: Parameter) {
        let configuration: Configuration = _.cloneDeep(this._config);
        let paramIndex = configuration.parameterSet.parameters.findIndex(param => param.ref.id === parameter.ref.id);
        if (paramIndex > -1) {
            configuration.parameterSet.parameters.splice(paramIndex, 1, parameter);
            this.onSave.emit(configuration);
            console.log("TSCHAKA:" ,configuration)
        }

    }

    ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }
}