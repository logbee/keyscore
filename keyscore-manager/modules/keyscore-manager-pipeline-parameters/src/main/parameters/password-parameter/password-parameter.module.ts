import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {PasswordParameterComponent} from "./password-parameter.component";
import {
    JSONCLASS_PASSWORD_DESCR,
    PasswordParameter,
    PasswordParameterDescriptor
} from "@keyscore-manager-models/src/main/parameters/password-parameter.model";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {StringValidatorService} from "../../service/string-validator.service";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        TranslateModule,
    ],
    declarations: [PasswordParameterComponent],
    entryComponents: [PasswordParameterComponent],
    exports: [PasswordParameterComponent],
    providers: [StringValidatorService]
})
export class PasswordParameterModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_PASSWORD_DESCR, (descriptor: PasswordParameterDescriptor, value?: string) => {
            return new PasswordParameter(descriptor.ref, value === null ? descriptor.defaultValue : (value === undefined ? '' : value));
        });
        this.componentFactory.register(JSONCLASS_PASSWORD_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(PasswordParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }
}
