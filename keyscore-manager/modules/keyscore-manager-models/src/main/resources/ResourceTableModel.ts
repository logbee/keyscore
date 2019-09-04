import {Configuration} from "../common/Configuration";
import {Blueprint} from "../blueprints/Blueprint";
import {FilterDescriptor} from "@keyscore-manager-models";

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