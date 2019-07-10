import {moduleMetadata, storiesOf} from "@storybook/angular";
import {action} from '@storybook/addon-actions';
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BooleanValueComponent} from "../main/value-controls/boolean-value.component";
import {MaterialModule} from "keyscore-manager-material";
import {ReactiveFormsModule} from "@angular/forms";
import {TextValueComponent} from "../main/value-controls/text-value.component";
import {TimestampValue} from "../main/models/value.model";
import {TimestampValueComponent} from "../main/value-controls/timestamp-value.component";

storiesOf('Values/BooleanValue', module)
    .addDecorator(
        moduleMetadata({
            declarations: [],
            imports: [
                CommonModule,
                MaterialModule,
                BrowserAnimationsModule,
                ReactiveFormsModule
            ],
            providers: []
        }))
    .add("default", () => ({
        component: BooleanValueComponent,
        props: {
            label: "Boolean Value",
            changed: action('Value Change')
        }
    })).add("disabled", () => ({
    component: BooleanValueComponent,
    props: {
        label: "Boolean Value",
        disabled: true,
        changed: action('Value Change')
    }
}));

storiesOf('Values/TextValue', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule,
            ReactiveFormsModule
        ],
        providers: []
    }))
    .add("default", () => ({
        component: TextValueComponent,
        props: {
            changed: action('Value Change')
        }

    }));

storiesOf('Values/TimestampValue', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            CommonModule,
            MaterialModule,
            BrowserAnimationsModule,
            ReactiveFormsModule
        ],
        providers: []
    }))
    .add("default", () => ({
        component: TimestampValueComponent,
        props: {
            changed: action('Value Change')
        }

    })).add("with initial date", () => ({
    component: TimestampValueComponent,
    props: {
        value:{seconds:1562690391,nanos:0},
        changed: action('Value Change')
    }

}));