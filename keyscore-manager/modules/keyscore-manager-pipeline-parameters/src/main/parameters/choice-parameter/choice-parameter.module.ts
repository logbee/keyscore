import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {ChoiceParameterComponent} from "./choice-parameter.component";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {
    ChoiceParameter,
    ChoiceParameterDescriptor,
    JSONCLASS_CHOICE_DESCR
} from "@keyscore-manager-models/src/main/parameters/choice-parameter.model";
import {ReactiveFormsModule} from "@angular/forms";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {MaterialModule} from "@/../modules/keyscore-manager-material/src/main/material.module";
import {TranslateModule} from "@ngx-translate/core";

@NgModule({
    imports:[
        CommonModule,
        MaterialModule,
        ReactiveFormsModule,
        TranslateModule
    ],
    declarations:[
        ChoiceParameterComponent
    ],
    entryComponents:[ChoiceParameterComponent],
    exports:[ChoiceParameterComponent]
})
export class ChoiceParameterModule{
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_CHOICE_DESCR, (descriptor: ChoiceParameterDescriptor, value?: string) => {
            return new ChoiceParameter(descriptor.ref, value === null ? "" : value);
        });
        this.componentFactory.register(JSONCLASS_CHOICE_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(ChoiceParameterComponent);
            return containerRef.createComponent(compFactory);
        });

    }
}