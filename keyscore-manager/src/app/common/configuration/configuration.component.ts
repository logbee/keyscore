import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {ResolvedParameterDescriptor} from "../../models/parameters/ParameterDescriptor";
import {Parameter} from "../../models/parameters/Parameter";
import {Configuration} from "../../models/common/Configuration";
import {FormBuilder, FormControl, FormGroup} from "@angular/forms";

@Component({
    selector: "configuration",
    template: `
        <form class="configuration" [formGroup]="form" *ngIf="form">
            <app-parameter *ngFor="let parameter of getKeys(parametersMapping)" [parameter]="parameter"
                           [parameterDescriptor]="parametersMapping.get(parameter)" [form]="form"></app-parameter>
        </form>

    `
})

export class ConfigurationComponent implements OnInit {
    @Input() parametersMapping: Map<Parameter,ResolvedParameterDescriptor>;

    @Output() configuration: EventEmitter<Configuration> = new EventEmitter();

    form:FormGroup;

    constructor() {

    }

    ngOnInit(): void {
        const group:any = {};

        this.parametersMapping.forEach((parameterDescriptor) => {
            group[parameterDescriptor.ref.uuid] = new FormControl();
        });

        this.form = new FormGroup(group);
    }

    getKeys(map:Map<any,any>){
        return Array.from(map.keys());
    }

}