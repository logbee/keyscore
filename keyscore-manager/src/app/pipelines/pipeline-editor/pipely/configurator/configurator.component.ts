import {Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {BehaviorSubject, Observable, Subject} from "rxjs";
import {BlockDescriptor} from "../models/block-descriptor.model";
import * as _ from "lodash";
import {
    Parameter,
    ParameterDescriptor,
    ParameterMap
} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";
import {Maturity} from "@keyscore-manager-models/src/main/descriptors/Maturity";
import {Dataset} from "@keyscore-manager-models/src/main/dataset/Dataset";
import {Agent} from "@keyscore-manager-models/src/main/common/Agent";
import {map, startWith, takeUntil} from "rxjs/operators";
import {ParameterFormComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameter-form.component"


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
<!--
    TODO: Implemented a feature-toggle to enable/disable this component.

                        <mat-form-field>
                            <mat-select formControlName="pipeline.selectedAgent" #agentSelect>
                                <mat-option>
                                    <ngx-mat-select-search
                                            [formControl]="filteredAgentsControl"></ngx-mat-select-search>
                                </mat-option>
                                <mat-option [value]="" *ngIf="!filteredAgentsControl.value">None</mat-option>
                                <mat-option *ngFor="let agent of filteredAgents$ | async" [value]="agent.id">
                                    {{agent.name}}
                                </mat-option>
                            </mat-select>
                            <mat-label>{{'CONFIGURATOR.SELECTED_AGENT' | translate}}</mat-label>
                        </mat-form-field>
-->
                    </form>
                </div>
            </div>
            <div *ngIf="(config$|async) as config" fxFlex>
                <ng-container *ngIf="config.conf">
                    <div fxLayout="column" fxLayoutGap="15px" fxLayoutAlign="start">
                        <div>
                            <div>
                                <h3 fxFlex style="margin-bottom: 5px">{{config?.descriptor?.displayName}}</h3>
                                <mat-icon *ngIf="showMaturityIcon(config?.descriptor.maturity)"
                                          [svgIcon]="maturityIconNameOf(config?.descriptor.maturity)"
                                          matTooltip="{{maturityTooltipOf(config?.descriptor.maturity) | translate}}">
                                </mat-icon>
                            </div>
                            <p style="margin-bottom: 0; font-family: monospace;font-size: small">{{config?.uuid}}</p>
                        </div>
                        <p>{{config?.descriptor?.description}}</p>
                        <mat-divider></mat-divider>
                        <div class="configurator-body">
                            <parameter-form #parameterForm [config]="parameterMap$|async"
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
    @Input() pipelineMetaData: { name: string, description: string, selectedAgent: string } = {
        name: "",
        description: "",
        selectedAgent: ""
    };

    @Input() agents: Agent[];

    @Input('config') set config(val: { conf: Configuration, descriptor: BlockDescriptor, uuid: string }) {
        if (val) {
            if(this.parameterForm){
                this.parameterForm.triggerInputChangeDetection();
            }
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
                    rec.fields.map(field => field.name)))))) as string[];
    }

    private autoCompleteOptions: string[] = [];

    @Output() closeConfigurator: EventEmitter<void> = new EventEmitter();
    @Output() onSave: EventEmitter<Configuration> = new EventEmitter();
    @Output() onRevert: EventEmitter<void> = new EventEmitter();
    @Output() onSavePipelineMetaData: EventEmitter<{ name: string, description: string, selectedAgent: string }> = new EventEmitter();
    @Output() onOverwriteConfiguration: EventEmitter<void> = new EventEmitter();

    @ViewChild('parameterForm') parameterForm: ParameterFormComponent;

    private config$ = new BehaviorSubject<{ conf: Configuration, descriptor: BlockDescriptor, uuid: string }>(undefined);
    private _config: Configuration;

    form: FormGroup;
    pipelineForm: FormGroup;

    filteredAgentsControl: FormControl = new FormControl();
    filteredAgents$: Observable<Agent[]>;

    parameterMap$: BehaviorSubject<{ id: string, parameters: ParameterMap }> = new BehaviorSubject(null);

    unsubscribe$: Subject<void> = new Subject();

    constructor() {
    }

    public ngOnInit(): void {

        this.config$.pipe(takeUntil(this.unsubscribe$)).subscribe((config: { conf: Configuration, descriptor: BlockDescriptor, uuid: string }) => {
            this.createParameterMap(config);
        });


        this.pipelineForm = new FormGroup({
            'pipeline.name': new FormControl(this.pipelineMetaData.name),
            'pipeline.description': new FormControl(this.pipelineMetaData.description),
            'pipeline.selectedAgent': new FormControl(this.pipelineMetaData.selectedAgent)
        });

        this.pipelineForm.valueChanges.pipe(takeUntil(this.unsubscribe$)).subscribe(val => {
            this.onSavePipelineMetaData.emit({
                name: val['pipeline.name'],
                description: val['pipeline.description'],
                selectedAgent: val['pipeline.selectedAgent']
            });
        });

        this.filteredAgents$ = this.filteredAgentsControl.valueChanges.pipe(
            startWith(''),
            map((val: string) => this.filterAgents(val)),
            takeUntil(this.unsubscribe$)
        )


    }


    private filterAgents(val: string): Agent[] {
        if (!this.agents || !this.agents.length) return [];
        if (!val) return this.agents;

        const resultAgents = _.cloneDeep(this.agents);
        const searchValue = val.toLowerCase();
        return resultAgents.filter(agent =>
            agent.id.toLowerCase().includes(searchValue) ||
            agent.name.toLowerCase().includes(searchValue) ||
            agent.host.toLowerCase().includes(searchValue))
    }

    private createParameterMap(config: { conf: Configuration, descriptor: BlockDescriptor, uuid: string }) {
        if (!config.conf) {
            this.parameterMap$.next(null);
            return;
        }
        const parameterTuples: [Parameter, ParameterDescriptor][] =
            _.zip(config.conf.parameterSet.parameters, config.descriptor.parameters);
        let parameterMap: ParameterMap = {parameters: {}};
        parameterTuples.forEach(([parameter, descriptor]) =>
            parameterMap.parameters[descriptor.ref.id] = [parameter, descriptor]);

        this.parameterMap$.next({id: config.uuid, parameters: parameterMap});
    }

    saveConfiguration(parameter: Parameter) {
        let configuration: Configuration = _.cloneDeep(this._config);
        let paramIndex = configuration.parameterSet.parameters.findIndex(param => param.ref.id === parameter.ref.id);
        if (paramIndex > -1) {
            configuration.parameterSet.parameters.splice(paramIndex, 1, parameter);
            this.onSave.emit(configuration);
        }
    }

    ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private showMaturityIcon(maturity: Maturity): boolean {
        return maturity && maturity != Maturity.None
    }

    private maturityIconNameOf(maturity: Maturity): string {
        return "maturity-" + maturity.toString().toLowerCase();
    }

    private maturityTooltipOf(maturity: Maturity): string {
        return "MATURITY." + maturity.toString().toUpperCase();
    }
}