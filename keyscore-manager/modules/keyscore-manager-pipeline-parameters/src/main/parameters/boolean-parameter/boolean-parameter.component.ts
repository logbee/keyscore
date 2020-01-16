import {Component} from "@angular/core";
import {ParameterComponent} from "../ParameterComponent";
import {
    BooleanParameterDescriptor,
    BooleanParameter
} from "@/../modules/keyscore-manager-models/src/main/parameters/boolean-parameter.model";

@Component({
    selector: 'parameter-boolean',
    template: `
        <div class="form-field-pb-20">
            <mat-slide-toggle [checked]="parameter.value" (change)="onChange($event.checked)">
                {{descriptor.displayName}}
            </mat-slide-toggle>
        </div>
    `,
})
export class BooleanParameterComponent extends ParameterComponent<BooleanParameterDescriptor, BooleanParameter> {

    private onChange(value: boolean): void {
        const parameter = new BooleanParameter(this.descriptor.ref, value);
        console.log("changed ", parameter);
        this.emit(parameter);
    }
}
