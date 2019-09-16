import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
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
import {DecimalValueComponent} from "./decimal-value.component";
//svg icons
import {
    boolean_icon,
    decimal_icon,
    duration_icon,
    number_icon,
    text_icon,
    timestamp_icon
} from "../../../assets/icons/values/value-icons";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";
import {TranslateModule} from "@ngx-translate/core";


@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        ReactiveFormsModule,
        FormsModule,
        SharedControlsModule,
        TranslateModule
    ],
    declarations: [
        ValueDirective,
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent,
        DurationValueComponent,
        NumberValueComponent,
        DecimalValueComponent

    ],
    exports: [
        ValueDirective,
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent,
        DurationValueComponent,
        NumberValueComponent,
        DecimalValueComponent
    ],
    entryComponents: [
        BooleanValueComponent,
        TextValueComponent,
        TimestampValueComponent,
        DurationValueComponent,
        NumberValueComponent,
        DecimalValueComponent

    ]
})
export class ValueControlsModule {

    constructor(private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer) {
        this.addCustomIcons();
    }

    private addCustomIcons() {
        this.matIconRegistry.addSvgIconLiteral('text-icon', this.domSanitizer.bypassSecurityTrustHtml(text_icon));
        this.matIconRegistry.addSvgIconLiteral('boolean-icon', this.domSanitizer.bypassSecurityTrustHtml(boolean_icon));
        this.matIconRegistry.addSvgIconLiteral('decimal-icon', this.domSanitizer.bypassSecurityTrustHtml(decimal_icon));
        this.matIconRegistry.addSvgIconLiteral('duration-icon', this.domSanitizer.bypassSecurityTrustHtml(duration_icon));
        this.matIconRegistry.addSvgIconLiteral('number-icon', this.domSanitizer.bypassSecurityTrustHtml(number_icon));
        this.matIconRegistry.addSvgIconLiteral('timestamp-icon', this.domSanitizer.bypassSecurityTrustHtml(timestamp_icon));

    }
}