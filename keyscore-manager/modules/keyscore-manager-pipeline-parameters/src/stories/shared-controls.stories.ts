import {moduleMetadata, storiesOf} from "@storybook/angular";
import {action} from '@storybook/addon-actions';
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ReactiveFormsModule} from "@angular/forms";
import {DurationInputComponent} from "../main/shared-controls/duration-input.component";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";

storiesOf('SharedControls/DurationInput', module)
    .addDecorator(
        moduleMetadata({
            declarations: [
                DurationInputComponent
            ],
            imports: [
                CommonModule,
                MaterialModule,
                BrowserAnimationsModule,
                ReactiveFormsModule
            ],
            providers: []
        }))
    .add("default", () => {
        return {
            template: `
            <mat-form-field>
                <ks-duration-input (changed)="changed($event)"></ks-duration-input>
                <mat-label>Duration</mat-label>
            </mat-form-field>
          `,
            props: {
                changed: action('Value Change')
            }
        }
    });