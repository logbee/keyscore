import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {Subject} from "rxjs";
import {RouterTestingModule} from "@angular/router/testing";
import {MaterialModule} from "../../app/material.module";
import {ConfigurationComponent} from "../../app/common/configuration/configuration.component";
import {ParameterDescriptorJsonClass} from "../../app/models/parameters/ParameterDescriptor";
import {generateParameter, generateResolvedParameterDescriptor} from "../fake-data/pipeline-fakes";
import {ParameterJsonClass} from "../../app/models/parameters/Parameter";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {ParameterComponent} from "../../app/common/configuration/parameter/parameter.component";
import {ParameterList} from "../../app/common/configuration/parameter/parameter-list.component";
import {ParameterMap} from "../../app/common/configuration/parameter/parameter-map.component";
import {HttpClient, HttpClientModule} from "@angular/common/http";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {HttpLoaderFactory} from "../../app/app.module";
import {zip} from "../../app/util";
import {BrowserAnimationsModule, NoopAnimationsModule} from "@angular/platform-browser/animations";

describe('ConfiguratorComponent', () => {
    let component: ConfigurationComponent;
    let fixture: ComponentFixture<ConfigurationComponent>;
    let unsubscribe = new Subject<void>();

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [
                ConfigurationComponent,
                ParameterComponent,
                ParameterList,
                ParameterMap
            ],
            imports: [
                NoopAnimationsModule,
                RouterTestingModule,
                MaterialModule,
                ReactiveFormsModule,
                HttpClientModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useFactory: HttpLoaderFactory,
                        deps: [HttpClient]
                    }
                }),
            ],
            providers: []
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ConfigurationComponent);
        component = fixture.componentInstance;
    });

    afterEach(() => {
        unsubscribe.next();
        unsubscribe.complete();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    const descriptorJsonClasses: ParameterDescriptorJsonClass[] = [ParameterDescriptorJsonClass.ExpressionParameterDescriptor,
        ParameterDescriptorJsonClass.ChoiceParameterDescriptor, ParameterDescriptorJsonClass.BooleanParameterDescriptor,
        ParameterDescriptorJsonClass.DecimalParameterDescriptor, ParameterDescriptorJsonClass.FieldListParameterDescriptor,
        ParameterDescriptorJsonClass.FieldNameListParameterDescriptor, ParameterDescriptorJsonClass.FieldNameParameterDescriptor,
        ParameterDescriptorJsonClass.FieldParameterDescriptor, ParameterDescriptorJsonClass.NumberParameterDescriptor,
        ParameterDescriptorJsonClass.TextListParameterDescriptor, ParameterDescriptorJsonClass.TextParameterDescriptor];

    const parameterJsonClasses = [ParameterJsonClass.ExpressionParameter, ParameterJsonClass.ChoiceParameter,
        ParameterJsonClass.BooleanParameter, ParameterJsonClass.DecimalParameter, ParameterJsonClass.FieldListParameter,
        ParameterJsonClass.FieldNameListParameter, ParameterJsonClass.FieldNameParameter, ParameterJsonClass.FieldParameter,
        ParameterJsonClass.NumberParameter, ParameterJsonClass.TextListParameter, ParameterJsonClass.TextParameter];

    describe('ngOnInit', () => {
        it('should create the form group with all given parameters', () => {
            let parameterDescriptors = descriptorJsonClasses.map(jsonClass => generateResolvedParameterDescriptor(jsonClass));
            let parameters = parameterJsonClasses.map(jsonClass => generateParameter(jsonClass));
            component.parametersMapping = new Map(zip([parameters, parameterDescriptors]));

            fixture.detectChanges();

            let hasValue = 0;
            component.parametersMapping.forEach((_,descriptor) => {
                hasValue = component.form.controls[descriptor.ref.uuid].value !== null ? hasValue + 1 : hasValue;
            });

            expect(hasValue).toBe(component.parametersMapping.size);
        })
    });
});