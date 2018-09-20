import {NgModule} from '@angular/core';

import {
    MatButtonModule,
    MatMenuModule,
    MatToolbarModule,
    MatIconModule,
    MatCardModule,
    MatGridListModule,
    MatListModule,
    MatDividerModule,
    MatTableModule,
    MatInputModule,
    MatFormFieldModule,
    MatSidenavModule,
    MatCheckboxModule,
    MatChipsModule,
    MatSlideToggleModule

} from '@angular/material';
import {FlexLayoutModule} from '@angular/flex-layout';


@NgModule({
    imports: [
        MatButtonModule,
        MatMenuModule,
        MatToolbarModule,
        MatIconModule,
        MatCardModule,
        MatGridListModule,
        MatListModule,
        MatDividerModule,
        FlexLayoutModule,
        MatTableModule,
        MatInputModule,
        MatFormFieldModule,
        MatSidenavModule,
        MatCheckboxModule,
        MatChipsModule,
        MatSlideToggleModule

    ],
    exports: [
        MatButtonModule,
        MatMenuModule,
        MatToolbarModule,
        MatIconModule,
        MatCardModule,
        MatGridListModule,
        MatListModule,
        MatDividerModule,
        FlexLayoutModule,
        MatTableModule,
        MatInputModule,
        MatFormFieldModule,
        MatSidenavModule,
        MatCheckboxModule,
        MatChipsModule,
        MatSlideToggleModule
    ]
})
export class MaterialModule {
}