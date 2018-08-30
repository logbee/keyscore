import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ParameterMap} from "./parameter-map.component";
import {ParameterList} from "./parameter-list.component";
import {ParameterComponent} from "./parameter.component";
import {ParameterControlService} from "./services/parameter-control.service";
import {TranslateModule} from "@ngx-translate/core";
import {MatInputModule} from "@angular/material/input";
import {MatTableModule} from "@angular/material/table";
import {MatIconModule} from "@angular/material/icon";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatButtonModule} from "@angular/material/button";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatChipsModule} from "@angular/material/chips";
import {FlexLayoutModule} from "@angular/flex-layout";

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MatTableModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatCheckboxModule,
        MatChipsModule,
        FlexLayoutModule,

    ],
    declarations: [
        ParameterMap,
        ParameterList,
        ParameterComponent
    ]
    ,
    exports:[
        ParameterMap,
        ParameterList,
        ParameterComponent
    ],
    providers: [ParameterControlService]
})
export class ParameterModule {
}
