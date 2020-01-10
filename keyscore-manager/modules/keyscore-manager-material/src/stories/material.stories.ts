import {ConfirmButtonComponent} from "@keyscore-manager-material/src/main/components/confirm-button.component";

import {moduleMetadata, storiesOf} from "@storybook/angular";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MatButtonModule} from "@angular/material/button";
import {action} from '@storybook/addon-actions';
import {MatIconModule} from "@angular/material/icon";

storiesOf('Material', module).addDecorator(
    moduleMetadata({
        declarations: [
            ConfirmButtonComponent,
        ],
        imports: [
            BrowserAnimationsModule,
            MatButtonModule,
            MatIconModule,
            CommonModule,
        ]
    }))
    .add("Confirm Button", () => ({
        template: `
            <div>
                <confirm-button (confirmed)="confirm($event)">Confirm</confirm-button>
                <span style="margin-left: 25px"></span>
                <confirm-button (confirmed)="edit($event)"><mat-icon>edit</mat-icon></confirm-button>
            </div>
            <div style="margin-top: 25px"></div>
            <div>
                <confirm-button kind="accept" (confirmed)="accept($event)">Accept</confirm-button>
                <span style="margin-left: 25px"></span>
                <confirm-button kind="accept" (confirmed)="play($event)"><mat-icon>play_arrow</mat-icon></confirm-button>
            </div>
            <div style="margin-top: 25px"></div>
            <div>
                <confirm-button kind="caution" (confirmed)="caution($event)">Caution</confirm-button>
                <span style="margin-left: 25px"></span>
                <confirm-button kind="caution" (confirmed)="delete($event)"><mat-icon>delete</mat-icon></confirm-button>
            </div>
        `,
        props: {
            confirm: action('confirm'),
            accept: action('accept'),
            caution: action('caution'),
            edit: action('edit'),
            play: action('play'),
            delete: action('delete'),
        },
    }));
