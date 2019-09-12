import {Blueprint} from "@keyscore-manager-models/src/main/blueprints/Blueprint";
import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";
import {FilterDescriptor} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";

export class ResourceTableModel {
    blueprint: Blueprint;
    configuration: Configuration;
    descriptor: FilterDescriptor;

    constructor(blueprint: Blueprint, configuration: Configuration, descriptor: FilterDescriptor) {
        this.blueprint = blueprint;
        this.configuration = configuration;
        this.descriptor = descriptor;
    }
}