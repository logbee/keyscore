import {Configuration} from "../common/Configuration";
import {Blueprint} from "../blueprints/Blueprint";
import {ResolvedFilterDescriptor} from "../descriptors/FilterDescriptor";

export class ResourceTableModel {
    blueprint: Blueprint;
    configuration: Configuration;
    descriptor: ResolvedFilterDescriptor;

    constructor(blueprint: Blueprint, configuration: Configuration, descriptor: ResolvedFilterDescriptor) {
        this.blueprint = blueprint;
        this.configuration = configuration;
        this.descriptor = descriptor;
    }
}