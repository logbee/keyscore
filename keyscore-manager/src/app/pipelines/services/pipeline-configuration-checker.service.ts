import {EditingPipelineModel} from "@keyscore-manager-models/src/main/pipeline-model/EditingPipelineModel";
import {FilterDescriptor} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";
import {Injectable} from "@angular/core";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import * as _ from 'lodash';
import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";

@Injectable({providedIn: 'root'})
export class PipelineConfigurationChecker {

    /**
     * Checks if the current editingPipelineModels configuration fits the current descriptor.
     * If this is not the case this method creates new parameter configurations with default values
     * on a copy of the original editingPipelineModel.
     * @param editingPipelineModel : EditingPipelineModel
     * @param pipelineFilterDescriptors : FilterDescriptor[]
     * @returns aligned EditingPipelineModel and a Map with block id as key and updated parameters as value
     */
    public alignConfigWithDescriptor(editingPipelineModel: EditingPipelineModel, pipelineFilterDescriptors: FilterDescriptor[]):
        { pipeline: EditingPipelineModel, updatedParameters: Map<string, ParameterDescriptor[]> } {

        const editingPipeline = _.cloneDeep(editingPipelineModel);
        const updatedParameters = new Map();

        editingPipeline.blueprints.forEach(blueprint => {
            const filterConfig = editingPipeline.configurations.find(config =>
                blueprint.configuration.uuid === config.ref.uuid);

            const filterDescriptor = pipelineFilterDescriptors.find(descriptor =>
                blueprint.descriptor.uuid === descriptor.descriptorRef.uuid);

            const [existingParameters, nonExistingParameters]: [ParameterDescriptor[], ParameterDescriptor[]] =
                _.partition(filterDescriptor.parameters, (parameter: ParameterDescriptor) =>
                    filterConfig.parameterSet.parameters.map(param => param.ref.id).includes(parameter.ref.id));

            filterConfig.parameterSet.parameters = this.addParameters(filterConfig.parameterSet.parameters, nonExistingParameters);

            const typeChangedParameterDescriptors: ParameterDescriptor[] = this.findTypeChangedDescriptors(existingParameters, filterConfig.parameterSet.parameters);

            filterConfig.parameterSet.parameters = this.alignParameterTypes(filterConfig.parameterSet.parameters, typeChangedParameterDescriptors);

            updatedParameters.set(blueprint.ref.uuid, [...nonExistingParameters, ...typeChangedParameterDescriptors]);

            filterConfig.parameterSet.parameters = this.removeUnnecessaryConfigurations(filterConfig.parameterSet.parameters, filterDescriptor.parameters);
        });

        return {pipeline: editingPipeline, updatedParameters: updatedParameters};
    }

    private findTypeChangedDescriptors(existingParameters: ParameterDescriptor[], parameterConfigs: Parameter[]) {
        return existingParameters.filter(descriptor => {
            const newParameter: Parameter = this.parameterFactory.parameterDescriptorToParameter(descriptor);
            const existingParameter: Parameter = parameterConfigs.find(param => param.ref.id === descriptor.ref.id);
            return newParameter.jsonClass !== existingParameter.jsonClass;
        });
    }

    private addParameters(parameters: Parameter[], parameterDescriptors: ParameterDescriptor[]): Parameter[] {
        const resultParameterList: Parameter[] = _.cloneDeep(parameters);
        const parametersToAdd: Parameter[] = parameterDescriptors.map(descriptor => this.parameterFactory.parameterDescriptorToParameter(descriptor));
        resultParameterList.push(...parametersToAdd);
        return resultParameterList;

    }

    private alignParameterTypes(parameters: Parameter[], typeChangedDescriptors: ParameterDescriptor[]): Parameter[] {
        let resultParameterList: Parameter[] = _.cloneDeep(parameters);
        resultParameterList = resultParameterList.filter(parameter => !typeChangedDescriptors.map(descriptor => descriptor.ref.id).includes(parameter.ref.id));
        const parametersToAdd = typeChangedDescriptors.map(descriptor => this.parameterFactory.parameterDescriptorToParameter(descriptor));
        resultParameterList.push(...parametersToAdd);
        return resultParameterList;
    }


    private removeUnnecessaryConfigurations(parameters: Parameter[], parameterDescriptors: ParameterDescriptor[]) {
        return parameters.filter(param => {
            return parameterDescriptors.map(desc => desc.ref.id).includes(param.ref.id);
        });
    }

    constructor(private parameterFactory: ParameterFactoryService) {

    }
}
