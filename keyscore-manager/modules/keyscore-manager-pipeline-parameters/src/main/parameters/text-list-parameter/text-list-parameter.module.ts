import {ComponentFactoryResolver, NgModule, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";
import {MaterialModule} from "keyscore-manager-material";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {ParameterFactoryService} from "../../service/parameter-factory.service";
import {ParameterComponentFactoryService} from "../../service/parameter-component-factory.service";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TextListParameterComponent} from "./text-list-parameter.component";
import {JSONCLASS_TEXTLIST_DESCR, TextListParameter, TextListParameterDescriptor} from "./text-list-parameter.model";
import {TextParameterModule} from "../text-parameter/text-parameter.module";

@NgModule({
    imports: [
        CommonModule,
        MaterialModule,
        BrowserAnimationsModule,
        DragDropModule,
        TextParameterModule
    ],
    declarations: [TextListParameterComponent],
    exports: [TextListParameterComponent],
    entryComponents: [TextListParameterComponent]
})
export class TextListParameterModule {

    constructor(private factory: ParameterFactoryService, private componentFactory: ParameterComponentFactoryService, private resolver: ComponentFactoryResolver) {
        this.factory.register(JSONCLASS_TEXTLIST_DESCR, (descriptor: TextListParameterDescriptor) => {
            return new TextListParameter(descriptor.ref, []);
        });

        this.componentFactory.register(JSONCLASS_TEXTLIST_DESCR, (containerRef: ViewContainerRef) => {
            const compFactory = this.resolver.resolveComponentFactory(TextListParameterComponent);
            return containerRef.createComponent(compFactory);
        });
    }

}