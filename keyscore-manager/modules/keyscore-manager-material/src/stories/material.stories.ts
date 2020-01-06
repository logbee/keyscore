import {ConfirmButtonComponent} from "@keyscore-manager-material/src/main/components/confirm-button.component";

import {moduleMetadata, storiesOf} from "@storybook/angular";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MatButtonModule} from "@angular/material/button";
import {action} from '@storybook/addon-actions';


storiesOf('Material', module).addDecorator(
    moduleMetadata({
        declarations: [],
        imports: [
            BrowserAnimationsModule,
            MatButtonModule,
            CommonModule,
        ]
    }))
    .add("ConfirmButton", () => ({
        component: ConfirmButtonComponent,
        // requiresComponentDeclaration: false, TODO: Enable after update of storybook >= 5.1, Fixes https://github.com/storybookjs/storybook/issues/5542
        props: {
            confirmed: action('Confirmed')
        },
    }));
