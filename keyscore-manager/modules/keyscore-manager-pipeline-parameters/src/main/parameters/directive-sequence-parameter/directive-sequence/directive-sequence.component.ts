import {
    AfterContentInit,
    AfterViewInit,
    Component, ElementRef,
    EventEmitter,
    Input,
    OnDestroy,
    Output, QueryList,
    ViewChild, ViewChildren,
    ViewContainerRef
} from "@angular/core";
import {
    FieldDirectiveSequenceParameterDescriptor,
    FieldDirectiveSequenceParameter,
    DirectiveConfiguration,
    FieldDirectiveSequenceConfiguration,
    FieldDirectiveDescriptor
} from '@keyscore-manager-models/src/main/parameters/directive.model';
import {ParameterDescriptor, Parameter} from '@keyscore-manager-models/src/main/parameters/parameter.model';
import {ParameterComponentFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-component-factory.service";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {
    AddDirectiveComponent,
    MenuItem
} from "@keyscore-manager-pipeline-parameters/src/main/parameters/directive-sequence-parameter/add-directive/add-directive.component";
import uuid = require("uuid");
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {animate, style, transition, trigger, AnimationEvent} from "@angular/animations";
import {ParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/ParameterComponent";
import {ParameterDescriptorJsonClass} from '@keyscore-manager-models/src/main/parameters/parameter.model'
import {ParameterGroupDescriptor} from '@keyscore-manager-models/src/main/parameters/group-parameter.model'
import {ParameterGroupComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/parameter-group/parameter-group.component";
import {ContentChildren} from "@angular/core/src/metadata/di";

@Component({
    selector: 'ks-directive-sequence',
    template: `
        <mat-expansion-panel>
            <mat-expansion-panel-header [collapsedHeight]="expansionHeight"
                                        [expandedHeight]="expansionHeight"
                                        fxLayout="row-reverse" fxLayoutGap="15px"
                                        fxLayoutAlign="space-between center">
                <div fxLayout="row" fxLayoutAlign="space-between center" class="directive-header-wrapper">
                    <div fxLayout="column" class="parameter-wrapper-ds" (click)="$event.stopPropagation()">
                        <ng-template #parameterContainer></ng-template>
                    </div>
                    <button mat-button matSuffix mat-icon-button aria-label="delete directive sequence"
                            fxFlexAlign="center"
                            (click)="delete($event)">
                        <mat-icon color="warn">delete</mat-icon>
                    </button>
                </div>
            </mat-expansion-panel-header>
            <div class="sequence-body" fxLayout="column" fxLayoutGap="8px">
                <mat-divider></mat-divider>

                <div cdkDropList class="directive-list" (cdkDropListDropped)="drop($event)" fxLayout="column"
                     fxLayoutAlign="start stretch" fxLayoutGap="8px">
                    <ks-directive class="directive-draggable"
                                  *ngFor="let directive of sequence.directives;trackBy:trackByFn"
                                  [configuration]="directive"
                                  [descriptor]="getDirectiveDescriptor(directive)" (onDelete)="deleteDirective($event)"
                                  (onChange)="directiveChange($event)"
                                  cdkDrag (mousedown)="closeAddPanel()"></ks-directive>
                </div>

                <ks-button-add-directive [itemsToAdd]="_menuItems"
                                         [autoClosePanelOnAdd]="true"
                                         (onAdd)="addDirective($event)"></ks-button-add-directive>

            </div>
        </mat-expansion-panel>
    `,
    styleUrls: ['./directive-sequence.component.scss'],
    animations: [
        trigger('items', [
            transition(':enter', [
                style({transform: 'scale(0.5)', opacity: 0}),
                animate('0.6s cubic-bezier(.8, -0.6, 0.2, 1.5)',
                    style({transform: 'scale(1)', opacity: 1}))
            ]),
            transition(':leave', [
                style({transform: 'scale(1)', opacity: 1, height: '*'}),
                animate('0.6s cubic-bezier(.8, -0.6, 0.2, 1.5)',
                    style({
                        transform: 'scale(0.5)', opacity: 0,
                        height: '0px', margin: '0px'
                    }))
            ])
        ])
    ]
})
export class DirectiveSequenceComponent implements AfterViewInit, OnDestroy {

    @Input() sequence: FieldDirectiveSequenceConfiguration;

    @Input() set descriptor(val: FieldDirectiveSequenceParameterDescriptor) {
        this._descriptor = val;
        this._menuItems = this.directivesToMenuItems();
    };

    get descriptor(): FieldDirectiveSequenceParameterDescriptor {
        return this._descriptor;
    }

    private _descriptor: FieldDirectiveSequenceParameterDescriptor;
    @Input() autoCompleteDataList: string[];

    @Output() onSequenceChange: EventEmitter<FieldDirectiveSequenceConfiguration> = new EventEmitter<FieldDirectiveSequenceConfiguration>();
    @Output() onDelete: EventEmitter<FieldDirectiveSequenceConfiguration> = new EventEmitter<FieldDirectiveSequenceConfiguration>();

    @ViewChild('parameterContainer', {read: ViewContainerRef}) parameterContainer: ViewContainerRef;
    @ViewChild(AddDirectiveComponent) addComponent: AddDirectiveComponent;

    private readonly PARAMETER_HEIGHT: number = 90;

    private _unsubscribe$: Subject<void> = new Subject<void>();
    private _menuItems: MenuItem[] = [];

    private _parameterComponents: Map<string, ParameterComponent<ParameterDescriptor, Parameter>> = new Map();
    private _expansionHeight = 0;

    constructor(private _parameterComponentFactory: ParameterComponentFactoryService, private _parameterFactory: ParameterFactoryService) {

    }

    ngAfterViewInit(): void {
        this.createComponents();
    }

    private createComponents() {
        this.parameterContainer.clear();
        this.descriptor.parameters.forEach(parameterDescriptor => this.createComponent(parameterDescriptor));
    }

    private createComponent(descriptor: ParameterDescriptor) {
        if (descriptor.jsonClass === ParameterDescriptorJsonClass.ParameterGroupDescriptor) {
            const groupDescriptor: ParameterGroupDescriptor = descriptor as ParameterGroupDescriptor;
            if (groupDescriptor.condition) {
                setTimeout(() => {
                    const groupComponent = this._parameterComponents.get(groupDescriptor.ref.id);
                    const conditionComponent = this._parameterComponents.get(groupDescriptor.condition.parameter.id);
                    if (!groupComponent || !conditionComponent) return;
                    (groupComponent as ParameterGroupComponent).conditionInput = conditionComponent.value;
                    conditionComponent.emitter.pipe(takeUntil(this._unsubscribe$)).subscribe((parameter: Parameter) => {
                        (groupComponent as ParameterGroupComponent).conditionInput = parameter;
                    });
                }, 0)
            }
        }

        const componentRef = this._parameterComponentFactory.createParameterComponent(descriptor.jsonClass, this.parameterContainer);
        componentRef.instance.descriptor = descriptor;
        componentRef.instance.parameter = this.sequence.parameters.parameters.find(param => param.ref.id === descriptor.ref.id);
        componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;

        componentRef.instance.emitter.pipe(takeUntil(this._unsubscribe$)).subscribe(parameter => this.onParameterChange(parameter));

        this._parameterComponents.set(descriptor.ref.id, componentRef.instance);
    }

    private sequenceChanged(sequence: FieldDirectiveSequenceConfiguration) {
        this.onSequenceChange.emit(sequence);
    }

    private onParameterChange(parameter: Parameter) {
        const index = this.sequence.parameters.parameters.findIndex(param => param.ref.id === parameter.ref.id);
        if (index < 0) {
            throw new Error(`[DirectiveSequenceComponent] Parameter ${parameter.ref.id} is not part of the current sequence`)
        }
        this.sequence.parameters.parameters.splice(index, 1, parameter);
        this.sequenceChanged(this.sequence);
    }

    private getDirectiveDescriptor(config: DirectiveConfiguration) {
        const directiveDescriptor = this.descriptor.directives.find(descriptor => descriptor.ref.uuid === config.ref.uuid);
        if (!directiveDescriptor) {
            throw new Error(`[DirectiveSequenceComponent] No descriptor found for DirectiveConfiguration: ${config.ref.uuid}`);
        }
        return directiveDescriptor;
    }

    private delete(event: MouseEvent) {
        event.stopPropagation();
        this.onDelete.emit(this.sequence);
    }

    private get expansionHeight() {
        const height = (this.descriptor.parameters.length) * this.PARAMETER_HEIGHT;
        return `${height}px`;
    }


    private drop(event: CdkDragDrop<DirectiveConfiguration[]>) {
        moveItemInArray(this.sequence.directives, event.previousIndex, event.currentIndex);
        this.sequenceChanged(this.sequence);
    }

    private directivesToMenuItems(): MenuItem[] {
        return this.descriptor.directives.map(directive => {
            return {
                id: directive.ref.uuid,
                displayName: directive.displayName,
                description: directive.description,
                icon: directive.icon
            }
        });
    }

    private addDirective(item: MenuItem) {
        const directive = this.descriptor.directives.find(directive => directive.ref.uuid === item.id);
        if (!directive) throw new Error(`[DirectiveSequenceComponent] Directive ${item.displayName} was not found in the FieldDirectiveSequenceDescriptor!`);
        this.sequence.directives.push({
            ref: directive.ref,
            instance: {
                uuid: uuid()
            },
            parameters: {
                parameters: directive.parameters.map(descriptor => this._parameterFactory.parameterDescriptorToParameter(descriptor))
            }
        });
        this.sequenceChanged(this.sequence);
    }

    private deleteDirective(conf: DirectiveConfiguration) {
        const index = this.sequence.directives.findIndex(directive => directive.instance.uuid === conf.instance.uuid);
        if (index > -1) {
            this.sequence.directives.splice(index, 1);
            this.sequenceChanged(this.sequence);
        }

    }

    private directiveChange(conf: DirectiveConfiguration) {
        const index = this.sequence.directives.findIndex(directive => directive.instance.uuid === conf.instance.uuid);
        if (index < 0) {
            throw new Error(`[DirectiveSequenceComponent] The directive instance which was updated by the DirectiveComponent could not be found in the current sequence`);
        }
        this.sequence.directives.splice(index, 1, conf);
        this.sequenceChanged(this.sequence);

    }

    private closeAddPanel() {
        this.addComponent.closePanel();
    }

    private trackByFn(index: number, item: DirectiveConfiguration) {
        return item.instance.uuid;
    }

    ngOnDestroy(): void {
        this._unsubscribe$.next();
        this._unsubscribe$.complete();
    }
}
