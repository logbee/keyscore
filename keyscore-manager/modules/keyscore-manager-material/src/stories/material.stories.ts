import {ConfirmButtonComponent} from "@keyscore-manager-material/src/main/components/confirm-button.component";

import {moduleMetadata, storiesOf} from "@storybook/angular";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MatButtonModule} from "@angular/material/button";
import {action} from '@storybook/addon-actions';
import {Component} from "@angular/core";
import {MatIconModule} from "@angular/material/icon";

@Component({
    selector: "",
    template: `
        <confirm-button>Confirm</confirm-button>

        <div style="margin-top: 25px"></div>

        <confirm-button kind="accept">Accept</confirm-button>

        <div style="margin-top: 25px"></div>

        <confirm-button kind="caution">Caution</confirm-button>

        <div style="margin-top: 25px"></div>

        <confirm-button><mat-icon>edit</mat-icon></confirm-button>

        <div style="margin-top: 25px"></div>

        <confirm-button kind="accept"><mat-icon>play_arrow</mat-icon></confirm-button>

        <div style="margin-top: 25px"></div>

        <confirm-button kind="caution"><mat-icon>delete</mat-icon></confirm-button>
    `,
})
class ConfirmButtonCollectionComponent {

}

storiesOf('Material', module).addDecorator(
    moduleMetadata({
        declarations: [
            ConfirmButtonComponent,
            ConfirmButtonCollectionComponent
        ],
        imports: [
            BrowserAnimationsModule,
            MatButtonModule,
            MatIconModule,
            CommonModule,
        ]
    }))
    .add("ConfirmButton", () => ({
        component: ConfirmButtonCollectionComponent,
        // requiresComponentDeclaration: false, TODO: Enable after update of storybook >= 5.1, Fixes https://github.com/storybookjs/storybook/issues/5542
        props: {
            confirmed: action('Confirmed')
        },
    }));
