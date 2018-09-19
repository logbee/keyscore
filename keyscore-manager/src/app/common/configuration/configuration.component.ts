import {Component, EventEmitter, Input, Output} from "@angular/core";
import {ResolvedParameterDescriptor} from "../../models/parameters/ParameterDescriptor";
import {Parameter} from "../../models/parameters/Parameter";
import {Configuration} from "../../models/common/Configuration";
import {ParameterControlService} from "./parameter/services/parameter-control.service";
import {FormGroup} from "@angular/forms";

@Component({
    selector: "configuration",
    template: `

    `
})

export class ConfigurationComponent {
    @Input() parameter: Parameter;
    @Input() parameterDescriptor: ResolvedParameterDescriptor;

    @Output() configuration: EventEmitter<Configuration> = new EventEmitter();

    form:FormGroup;

    constructor(private parameterService: ParameterControlService) {

    }
}