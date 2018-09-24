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
    MatSlideToggleModule,
    MatTooltipModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatPaginatorModule,
    MatSortModule,
    MatTableDataSource

} from '@angular/material';
import {FlexLayoutModule} from '@angular/flex-layout';
import {HttpClientModule} from "@angular/common/http";


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
        MatSlideToggleModule,
        MatTooltipModule,
        MatSelectModule,
        HttpClientModule,
        MatPaginatorModule,
        MatSortModule,
        MatAutocompleteModule,



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
        MatSlideToggleModule,
        MatTooltipModule,
        MatSelectModule,
        HttpClientModule,
        MatPaginatorModule,
        MatSortModule,
        MatAutocompleteModule,
    ]
})
export class MaterialModule {
}