import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";
import {BehaviorSubject, Subject, Subscription} from "rxjs";
import {filter} from "rxjs/operators";
import {Parameter, ParameterJsonClass,ResolvedParameterDescriptor,Dataset,Configuration} from "keyscore-manager-models";
import {ParameterControlService} from "keyscore-manager-pipeline-parameters";
import {BlockDescriptor} from "../models/block-descriptor.model";
import {takeUntil} from "rxjs/internal/operators";
import * as _ from "lodash";


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
        </div>
    `
})

export class ConfiguratorComponent implements OnInit, OnDestroy {
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
        configuration: {ref: {uuid: "init"}, parent: null, parameterSet: {jsonClass:ParameterJsonClass.ParameterSet,parameters:[]}},
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
    isAlive: Subject<void> = new Subject();
    form: FormGroup;
    pipelineForm: FormGroup;
    parameterMapping: Map<Parameter, ResolvedParameterDescriptor> = new Map();

    private lastID: string = "";
    private lastValues = null;
    private formSubscription: Subscription;

    constructor(private parameterService: ParameterControlService) {
    }

    public ngOnInit(): void {

        this.selectedBlock$.pipe(takeUntil(this.isAlive), filter(block => block.configuration.ref.uuid !== this.lastID)).subscribe(selectedBlock => {
            if(this.formSubscription){
                this.formSubscription.unsubscribe();
            }
            this.lastID = selectedBlock.configuration.ref.uuid;

            this.parameterMapping =
                new Map(_.zip(selectedBlock.configuration.parameterSet.parameters,
                    selectedBlock.descriptor.parameters));
            if (this.form) {
                this.form.reset();
            }
            this.form = this.parameterService.toFormGroup(this.parameterMapping);

            this.formSubscription = this.form.valueChanges.subscribe(values => {
                if (!this.isAllNullOrEmpty(values) && !_.isEqual(this.lastValues, values) ) {
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
        this.selectedBlock$.getValue().configuration.parameterSet.parameters.forEach(parameter => {
                this.form.controls[parameter.ref.id].setValue(parameter.value);
            }
        );
        this.closeConfigurator.emit();
    }

    collapse() {
        this.isVisible = !this.isVisible;
        this.onShowConfigurator.emit(this.isVisible);
    }

    revert() {
        this.reset();
        this.onRevert.emit()
    }

    saveConfiguration() {
        let configuration: Configuration = _.cloneDeep(this.selectedBlock$.getValue().configuration);
        if (configuration.ref.uuid !== 'init') {
            configuration.parameterSet.parameters.forEach((parameter) => {
                if (this.form.controls[parameter.ref.id]) {
                    parameter.value = this.form.controls[parameter.ref.id].value;
                }
            });
            this.onSave.emit(configuration);
        }
    }


    getKeys(map: Map<any, any>): any[] {
        return Array.from(map.keys());
    }

    ngOnDestroy(): void {
        this.isAlive.next();
    }
}