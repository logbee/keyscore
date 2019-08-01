import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MaterialModule} from "keyscore-manager-material";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BooleanValueComponent} from "./boolean-value.component";
import {TextValueComponent} from "./text-value.component";
import {TimestampValueComponent} from "./timestamp-value.component";
import {ValueDirective} from "./directives/value.directive";
import {DurationValueComponent} from "./duration-value.component";
import {SharedControlsModule} from "../shared-controls/shared-controls.module";
import {NumberValueComponent} from "./number-value.component";
import {MatIconRegistry} from "@angular/material";
import {DomSanitizer} from "@angular/platform-browser";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule,
        ReactiveFormsModule,
        FormsModule,
        SharedControlsModule
    ],
    declarations: [
        ValueDirective,
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent,
        DurationValueComponent,
        NumberValueComponent,
    ],
    exports: [
        ValueDirective,
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent,
        DurationValueComponent,
        NumberValueComponent
    ],
    entryComponents: [
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent,
        DurationValueComponent,
        NumberValueComponent
    ]
})
export class ValueControlsModule {

    constructor(private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer) {
        this.addCustomIcons();
    }

    private addCustomIcons() {
        this.matIconRegistry.addSvgIcon('text-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("../../assets/icons/values/text-value.svg"));
        this.matIconRegistry.addSvgIcon('boolean-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("../../assets/icons/values/boolean-value.svg"));
        this.matIconRegistry.addSvgIcon('decimal-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("../../assets/icons/values/decimal-value.svg"));
        this.matIconRegistry.addSvgIcon('duration-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("../../assets/icons/values/duration-value.svg"));
        this.matIconRegistry.addSvgIconLiteral('number-icon', this.domSanitizer.bypassSecurityTrustHtml(require("raw-loader!../../../assets/icons/values/number-value.svg")));//
        this.matIconRegistry.addSvgIcon('timestamp-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("../../assets/icons/values/timestamp-value.svg"));

    }
}