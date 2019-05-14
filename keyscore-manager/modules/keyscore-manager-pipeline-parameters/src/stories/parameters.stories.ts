import {Dataset, ParameterDescriptorJsonClass, ParameterJsonClass, ValueJsonClass} from "keyscore-manager-models";
import {moduleMetadata, storiesOf} from "@storybook/angular";
import {AutocompleteInputComponent,} from "../main/autocomplete-input.component";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {CommonModule} from "@angular/common";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {PropagationStopModule} from "ngx-propagation-stop";
import {HttpClient} from "@angular/common/http";
import {generateResolvedParameterDescriptor} from "keyscore-manager-test-fixtures";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {MaterialModule} from "keyscore-manager-material";
import {ParameterMap} from "../main/parameter-map.component";
import {ParameterListComponent} from "../main/parameter-list.component";
import {ParameterComponent} from "../main/parameter.component";
import {ParameterDirectiveComponent} from "../main/parameter-directive.component";
import {ParameterControlService} from "../main/service/parameter-control.service";
import {ParameterFactoryService} from "../main/service/parameter-factory.service";

const exampleDatasets: Dataset[] = [{
    metaData: null,
    records: [
        {
            fields: [
                {
                    name: "message",
                    value: {
                        jsonClass: ValueJsonClass.TextValue,
                        value: "testValue1"
                    }
                },
                {
                    name: "measurement",
                    value: {
                        jsonClass: ValueJsonClass.TextValue,
                        value: "125.265"
                    }
                }
            ]
        }
    ]
}];

export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http);
}

storiesOf('Parameter', module).addDecorator(
    moduleMetadata({
        declarations: [ParameterMap,
            ParameterListComponent,
            ParameterComponent,
            AutocompleteInputComponent,
            ParameterDirectiveComponent],
        imports: [BrowserAnimationsModule,
            CommonModule,
            FormsModule,
            ReactiveFormsModule,
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useFactory: HttpLoaderFactory,
                    deps: [HttpClient]
                }
            }),
            MaterialModule,
            DragDropModule,
            PropagationStopModule],
        providers: [ParameterControlService, ParameterFactoryService]
    })).add('TextParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.TextParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: "InitialValue", jsonClass: ParameterJsonClass.TextParameter},
        form: new FormGroup({"testID": new FormControl("InitialValue")})
    }
})).add('NumberParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.NumberParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: 10, jsonClass: ParameterJsonClass.NumberParameter},
        form: new FormGroup({"testID": new FormControl(5)})
    }
})).add('DecimalParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.DecimalParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: 1.0, jsonClass: ParameterJsonClass.DecimalParameter},
        form: new FormGroup({"testID": new FormControl(1.0)})
    }
})).add('BooleanParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.BooleanParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: true, jsonClass: ParameterJsonClass.BooleanParameter},
        form: new FormGroup({"testID": new FormControl(true)})
    }
})).add('ChoiceParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.ChoiceParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: "choose...", jsonClass: ParameterJsonClass.ChoiceParameter},
        form: new FormGroup({"testID": new FormControl("choose...")})
    }
})).add('ExpressionParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.ExpressionParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: "", jsonClass: ParameterJsonClass.ExpressionParameter},
        form: new FormGroup(({"testID": new FormControl("")}))
    }
})).add('FieldNameParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.FieldNameParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: "", jsonClass: ParameterJsonClass.FieldNameParameter},
        form: new FormGroup({"testID": new FormControl("")}),
        datasets: exampleDatasets
    }
})).add('FieldParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.FieldParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: "", jsonClass: ParameterJsonClass.FieldParameter},
        form: new FormGroup(({"testID": new FormControl("")}))
    }
})).add('TextListParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.TextListParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: ["initial"], jsonClass: ParameterJsonClass.TextListParameter},
        form: new FormGroup(({"testID": new FormControl(["initial"])}))
    }
})).add('FieldNameListParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.FieldNameListParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: [], jsonClass: ParameterJsonClass.FieldNameListParameter},
        form: new FormGroup(({"testID": new FormControl([])})),
        datasets: exampleDatasets
    }
})).add('FieldListParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.FieldListParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: [], jsonClass: ParameterJsonClass.FieldListParameter},
        form: new FormGroup(({"testID": new FormControl([])}))
    }
})).add('DirectiveSequenceParameter', () => ({
    component: ParameterComponent,
    props: {
        parameterDescriptor: generateResolvedParameterDescriptor(ParameterDescriptorJsonClass.FieldDirectiveSequenceParameterDescriptor),
        parameter: {ref: {id: "testID"}, value: [], jsonClass: ParameterJsonClass.FieldDirectiveSequenceParameter},
        form: new FormGroup(({"testID": new FormControl([])}))
    }
}));