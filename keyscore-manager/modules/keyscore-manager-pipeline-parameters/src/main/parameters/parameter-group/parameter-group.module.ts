import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {ParameterGroupComponent} from "@keyscore-manager-pipeline-parameters/src/main/parameters/parameter-group/parameter-group.component";
import {CommonModule} from "@angular/common";
import {TranslateModule} from "@ngx-translate/core";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {ParameterComponentFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-component-factory.service";
import {
    JSONCLASS_GROUP_DESCR, ParameterGroup,
    ParameterGroupDescriptor
} from "@keyscore-manager-models/src/main/parameters/group-parameter.model";
import {Parameter} from "@keyscore-manager-models/src/main/parameters/parameter.model";

@NgModule({
    imports: [
        CommonModule,
        TranslateModule
    ],
    declarations: [
        ParameterGroupComponent
    ],
    exports: [
        ParameterGroupComponent
    ],
    entryComponents: [
        ParameterGroupComponent
    ]
})
export class ParameterGroupModule {
    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_GROUP_DESCR, (descriptor: ParameterGroupDescriptor, value: Parameter[] = []) => {
            return new ParameterGroup(descriptor.ref, value);
        });

        this.componentFactory.register(JSONCLASS_GROUP_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(ParameterGroupComponent);
            return containerRef.createComponent(compFactory);
        })
    }
}