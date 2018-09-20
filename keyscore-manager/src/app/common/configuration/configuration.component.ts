import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {ResolvedParameterDescriptor} from "../../models/parameters/ParameterDescriptor";
import {Parameter} from "../../models/parameters/Parameter";
import {Configuration} from "../../models/common/Configuration";
import {FormBuilder, FormControl, FormGroup} from "@angular/forms";

@Component({
    selector: "configuration",
    template: `
        <div class="configuration">
            <app-parameter *ngFor="let parameter of parameters; let i = index" [parameter]="parameter"
                           [parameterDescriptor]="parameterDescriptors[i]"></app-parameter>
        </div>

    `
})

export class ConfigurationComponent implements OnInit {
    @Input() parameters: Parameter[];
    @Input() parameterDescriptors: ResolvedParameterDescriptor[];

    @Output() configuration: EventEmitter<Configuration> = new EventEmitter();

    form:FormGroup;

    constructor(private formBuilder:FormBuilder) {

    }

    ngOnInit(): void {
        const group:any = {};
        this.parameterDescriptors.forEach(parameterDescriptor => {
            group[parameterDescriptor.ref.uuid] = new FormControl();
        });
        this.form = new FormGroup(group);
    }

}