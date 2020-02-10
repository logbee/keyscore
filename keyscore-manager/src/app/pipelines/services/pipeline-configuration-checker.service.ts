import {EditingPipelineModel} from "@keyscore-manager-models/src/main/pipeline-model/EditingPipelineModel";
import {FilterDescriptor} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";
import {Injectable} from "@angular/core";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import * as _ from 'lodash';
import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";
import {Blueprint} from "@keyscore-manager-models/src/main/blueprints/Blueprint";

@Injectable({providedIn: 'root'})
export class PipelineConfigurationChecker {

    /**
     * Align the FilterConfigurations of the pipeline model with the FilterDescriptors,
     * if the Configuration diverges from the Descriptor.
     * @param editingPipelineModel : EditingPipelineModel
     * @param pipelineFilterDescriptors : FilterDescriptor[]
     * @returns aligned EditingPipelineModel and a Map with blueprint id as key and updated ParameterDescriptors as value : {pipeline,updatedParameters}
     */
    public alignConfigWithDescriptor(editingPipelineModel: EditingPipelineModel, pipelineFilterDescriptors: FilterDescriptor[]):
        { pipeline: EditingPipelineModel, updatedParameters: Map<string, ParameterDescriptor[]> } {

        if (!editingPipelineModel) return {pipeline: null, updatedParameters: null};
        if (!pipelineFilterDescriptors || !pipelineFilterDescriptors.length) return {
            pipeline: editingPipelineModel,
            updatedParameters: new Map()
        };

        const editingPipeline = _.cloneDeep(editingPipelineModel);
        let updatedParameters = new Map();

        editingPipeline.blueprints.forEach(blueprint => {
            const filterConfig = this.findFilterConfiguration(blueprint, editingPipeline.configurations);
            const filterDescriptor = this.findFilterDescriptor(blueprint, pipelineFilterDescriptors);

            updatedParameters = this.addRecordForParametersToChange(updatedParameters, blueprint, filterConfig, filterDescriptor);

            filterConfig.parameterSet.parameters = this.addMissingParameters(filterConfig, filterDescriptor);
            filterConfig.parameterSet.parameters = this.alignParameterTypes(filterConfig, filterDescriptor);
            filterConfig.parameterSet.parameters = this.removeUnnecessaryParameterConfigurations(filterConfig, filterDescriptor);

        });

        return {pipeline: editingPipeline, updatedParameters: updatedParameters};
    }


    private addRecordForParametersToChange(updatedParameters: Map<string, ParameterDescriptor[]>, blueprint: Blueprint, config: Configuration, descriptor: FilterDescriptor): Map<string, ParameterDescriptor[]> {
        const resultMap = _.cloneDeep(updatedParameters);
        const missingParameters = this.getDescriptorsOfMissingParametersInConfig(descriptor, config);
        const notMissingParameters = this.getDescriptorsOfNotMissingParametersInConfig(descriptor, config);
        const typeChangedParameters = this.findTypeChangedDescriptors(notMissingParameters, config.parameterSet.parameters);

        resultMap.set(blueprint.ref.uuid, [...typeChangedParameters, ...missingParameters]);
        return resultMap;

    }

    private getDescriptorsOfMissingParametersInConfig(filterDescriptor: FilterDescriptor, filterConfig: Configuration): ParameterDescriptor[] {
        return _.partition(filterDescriptor.parameters, (parameter: ParameterDescriptor) =>
            filterConfig.parameterSet.parameters.map(param => param.ref.id).includes(parameter.ref.id))[1] as ParameterDescriptor[];
    }

    private getDescriptorsOfNotMissingParametersInConfig(filterDescriptor: FilterDescriptor, filterConfig: Configuration): ParameterDescriptor[] {
        return _.partition(filterDescriptor.parameters, (parameter: ParameterDescriptor) =>
            filterConfig.parameterSet.parameters.map(param => param.ref.id).includes(parameter.ref.id))[0] as ParameterDescriptor[];
    }

    private findFilterDescriptor(blueprint: Blueprint, descriptors: FilterDescriptor[]) {
        return descriptors.find(descriptor =>
            blueprint.descriptor.uuid === descriptor.descriptorRef.uuid);
    }

    private findFilterConfiguration(blueprint: Blueprint, configurations: Configuration[]) {
        return configurations.find(config =>
            blueprint.configuration.uuid === config.ref.uuid);
    }

    private findTypeChangedDescriptors(existingParameters: ParameterDescriptor[], parameterConfigs: Parameter[]): ParameterDescriptor[] {
        return existingParameters.filter(descriptor => {
            const newParameter: Parameter = this.parameterFactory.parameterDescriptorToParameter(descriptor);
            const existingParameter: Parameter = parameterConfigs.find(param => param.ref.id === descriptor.ref.id);
            return newParameter.jsonClass !== existingParameter.jsonClass;
        });
    }

    private addMissingParameters(config: Configuration, descriptor: FilterDescriptor): Parameter[] {
        const missingParameterDescriptors: ParameterDescriptor[] = this.getDescriptorsOfMissingParametersInConfig(descriptor, config);
        const resultParameterList: Parameter[] = _.cloneDeep(config.parameterSet.parameters);
        const parametersToAdd: Parameter[] = missingParameterDescriptors.map(descriptor => this.parameterFactory.parameterDescriptorToParameter(descriptor));
        resultParameterList.push(...parametersToAdd);
        return resultParameterList;

    }

    private alignParameterTypes(config: Configuration, descriptor: FilterDescriptor): Parameter[] {
        let resultParameterList: Parameter[] = _.cloneDeep(config.parameterSet.parameters);
        const typeChangedParameterDescriptors: ParameterDescriptor[] =
            this.findTypeChangedDescriptors(this.getDescriptorsOfNotMissingParametersInConfig(descriptor, config), config.parameterSet.parameters);
        resultParameterList = resultParameterList.filter(parameter => !typeChangedParameterDescriptors.map(descriptor => descriptor.ref.id).includes(parameter.ref.id));
        const parametersToAdd = typeChangedParameterDescriptors.map(descriptor => this.parameterFactory.parameterDescriptorToParameter(descriptor));
        resultParameterList.push(...parametersToAdd);
        return resultParameterList;
    }


    private removeUnnecessaryParameterConfigurations(config: Configuration, descriptor: FilterDescriptor): Parameter[] {
        return config.parameterSet.parameters.filter(param => {
            return descriptor.parameters.map(desc => desc.ref.id).includes(param.ref.id);
        });
    }

    constructor(private parameterFactory: ParameterFactoryService) {

    }
}
