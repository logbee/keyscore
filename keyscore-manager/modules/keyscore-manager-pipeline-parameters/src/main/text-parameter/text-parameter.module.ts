import {NgModule} from "@angular/core";
import {ParameterFactoryService} from "../service/parameter-factory.service";
import {ResolvedParameterDescriptor, ParameterJsonClass,Parameter,SingleResolvedParameterDescriptor} from "keyscore-manager-models"
import {ParameterControlService} from "../service/parameter-control.service";
import {FormControl, Validators} from "@angular/forms";

@NgModule({
    imports: [],
    declarations: [],

})
export class TextParameterModule {
    constructor(private factory: ParameterFactoryService,private controlFactory: ParameterControlService) {
        this.factory.register("io.logbee.keyscore.model.descriptor.TextParameterDescriptor", (descriptor: ResolvedParameterDescriptor) => {
            console.log("Resolved TextParameter");
            return {ref: descriptor.ref, value: null, jsonClass: ParameterJsonClass.TextParameter};

        });

    }
}