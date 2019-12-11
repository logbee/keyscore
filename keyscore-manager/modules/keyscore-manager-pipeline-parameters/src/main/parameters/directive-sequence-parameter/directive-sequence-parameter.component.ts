import {Component} from "@angular/core";
import {ParameterComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/ParameterComponent";
import {
    FieldDirectiveSequenceParameterDescriptor,
    FieldDirectiveSequenceParameter,
    DirectiveConfiguration
} from '@keyscore-manager-models/src/main/parameters/directive.model';


@Component({
    selector: 'ks-parameter-directive-sequence',
    template: `
        <mat-expansion-panel>
            <mat-expansion-panel-header>
                <mat-panel-title>
                    {{descriptor.displayName}}
                </mat-panel-title>
            </mat-expansion-panel-header>
            
        </mat-expansion-panel>
    `,
    styleUrls: ['./directive-sequence-parameter.component.scss']
})
export class DirectiveSequenceParameterComponent extends ParameterComponent<FieldDirectiveSequenceParameterDescriptor, FieldDirectiveSequenceParameter> {

    private getDirectiveDescriptor(config: DirectiveConfiguration) {
        const directiveDescriptor = this.descriptor.directives.find(descriptor => descriptor.ref.uuid === config.ref.uuid);
        if (!directiveDescriptor) {
            throw new Error(`[DirectiveSequenceComponent] No descriptor found for DirectiveConfiguration: ${config.ref.uuid}`);
        }
        return directiveDescriptor;
    }


}
