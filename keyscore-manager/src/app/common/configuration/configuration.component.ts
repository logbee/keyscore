import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {ResolvedParameterDescriptor} from "../../models/parameters/ParameterDescriptor";
import {Parameter} from "../../models/parameters/Parameter";
import {Configuration} from "../../models/common/Configuration";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Observable} from "rxjs";
import {generateRef} from "../../models/common/Ref";

@Component({
    selector: "configuration",
    template: `
        <form class="configuration" *ngIf="form" [formGroup]="form">
            <app-parameter *ngFor="let parameter of getKeys(parametersMapping)" [parameter]="parameter"
                           [parameterDescriptor]="parametersMapping.get(parameter)" [form]="form"></app-parameter>
        </form>

    `
})

export class ConfigurationComponent implements OnInit,AfterViewInit {
    @Input() parametersMapping$: Observable<Map<Parameter, ResolvedParameterDescriptor>>;
    @Input() configuration: Configuration = null;
    @Input() saveConfiguration$: Observable<any>;

    @Output() updateConfiguration: EventEmitter<Configuration> = new EventEmitter();

    form: FormGroup;
    parametersMapping: Map<Parameter, ResolvedParameterDescriptor> = new Map();

    constructor() {

    }

    ngOnInit(): void {
        this.parametersMapping$.subscribe(parameterMap => {
            this.parametersMapping = parameterMap;
            this.initialiseForm(parameterMap);
        });
    }

    ngAfterViewInit(){
        this.saveConfiguration$.subscribe(event => {
            let ref = this.configuration ? this.configuration.ref : generateRef();
            let parent = this.configuration ? this.configuration.parent : null;
            this.parametersMapping.forEach((descriptor, parameter) => {
                parameter.value = this.form.controls[descriptor.ref.uuid].value;
            });

            this.updateConfiguration.emit({
                ref: ref,
                parent: parent,
                parameters: Array.from(this.parametersMapping.keys())
            });
        });
    }

    initialiseForm(parameterMap: Map<Parameter, ResolvedParameterDescriptor>) {
        const group: any = {};

        parameterMap.forEach((parameterDescriptor, parameter) => {
            group[parameterDescriptor.ref.uuid] = new FormControl(parameter.value);
        });

        this.form = new FormGroup(group);
        console.log(this.form);
    }

    getKeys(map: Map<any, any>) {
        return Array.from(map.keys());
    }

}