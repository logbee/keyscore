import {Component} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {BooleanParameter, BooleanParameterDescriptor} from "./boolean-parameter.model";

@Component({
    selector: 'parameter-boolean',
    template: `
            <mat-slide-toggle [checked]="parameter.value" (change)="onChange($event.checked)">
                {{descriptor.displayName}}
            </mat-slide-toggle>`,
})
export class BooleanParameterComponent extends ParameterComponent<BooleanParameterDescriptor, BooleanParameter> {

    private onChange(value: boolean): void {
        const parameter = new BooleanParameter(this.descriptor.ref, value);
        console.log("changed ", parameter);
        this.emit(parameter);
    }
}