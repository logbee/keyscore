import {EditingPipelineModel} from "@keyscore-manager-models/src/main/pipeline-model/EditingPipelineModel";
import {FilterDescriptor} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";
import {Injectable} from "@angular/core";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {cloneDeep,partition} from 'lodash-es';
import {Parameter, ParameterDescriptor} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";
import {Blueprint} from "@keyscore-manager-models/src/main/blueprints/Blueprint";
import {BehaviorSubject, Observable} from "rxjs";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";
import {map} from "rxjs/operators";

@Injectable({providedIn: 'root'})
export class PipelineConfigurationChecker {

    private _changedParameters$: BehaviorSubject<Map<string, ParameterRef[]>> = new BehaviorSubject<Map<string, ParameterRef[]>>(new Map());
    public changedParameters$: Observable<Map<string, ParameterRef[]>> = this._changedParameters$.asObservable();

    public alignConfigWithDescriptor(editingPipelineModel: EditingPipelineModel, pipelineFilterDescriptors: FilterDescriptor[]): EditingPipelineModel {

        if (!editingPipelineModel) return null;
        if (!pipelineFilterDescriptors || !pipelineFilterDescriptors.length) {
            this._changedParameters$.next(new Map());
            return cloneDeep(editingPipelineModel);
        }

        const editingPipeline = cloneDeep(editingPipelineModel);
        const updatedParameters: Map<string, ParameterRef[]> = new Map();

        editingPipeline.blueprints.forEach(blueprint => {
            const filterConfig = this.findFilterConfiguration(blueprint, editingPipeline.configurations);
            const filterDescriptor = this.findFilterDescriptor(blueprint, pipelineFilterDescriptors);

            updatedParameters.set(blueprint.ref.uuid, this.getChangedParametersForConfigAndDescriptor(filterConfig, filterDescriptor));

            filterConfig.parameterSet.parameters = this.addMissingParameters(filterConfig, filterDescriptor);
            filterConfig.parameterSet.parameters = this.alignParameterTypes(filterConfig, filterDescriptor);
            filterConfig.parameterSet.parameters = this.removeUnnecessaryParameterConfigurations(filterConfig, filterDescriptor);

        });
        this._changedParameters$.next(updatedParameters);

        return editingPipeline;
    }

    private addMissingParameters(config: Configuration, descriptor: FilterDescriptor): Parameter[] {
        const missingParameterDescriptors: ParameterDescriptor[] = this.getDescriptorsOfMissingParametersInConfig(descriptor, config);
        const resultParameterList: Parameter[] = cloneDeep(config.parameterSet.parameters);
        const parametersToAdd: Parameter[] = missingParameterDescriptors.map(descriptor => this.parameterFactory.parameterDescriptorToParameter(descriptor));
        resultParameterList.push(...parametersToAdd);
        return resultParameterList;

    }

    private alignParameterTypes(config: Configuration, descriptor: FilterDescriptor): Parameter[] {
        let resultParameterList: Parameter[] = cloneDeep(config.parameterSet.parameters);
        const typeChangedParameterDescriptors: ParameterDescriptor[] =
            this.getDescriptorsOfParametersWhereTypeChanged(this.getDescriptorsOfExistingParametersInConfig(descriptor, config), config.parameterSet.parameters);
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

    public confirmChangedParametersForFilter(filterBlueprintRef: string) {
        const changedParameters: Map<string, ParameterRef[]> = cloneDeep(this._changedParameters$.getValue());
        changedParameters.set(filterBlueprintRef, []);
        this._changedParameters$.next(changedParameters);
    }

    public getUpdatedParametersForFilter(filterBlueprintRef: string): Observable<ParameterRef[]> {
        return this.changedParameters$.pipe(map(changedParameters => {
            const parameters = changedParameters.get(filterBlueprintRef);
            return parameters || [];
        }));
    }

    private getChangedParametersForConfigAndDescriptor(config: Configuration, descriptor: FilterDescriptor): ParameterRef[] {
        const existingParameters = this.getDescriptorsOfExistingParametersInConfig(descriptor, config);
        const missingParameters = this.getDescriptorsOfMissingParametersInConfig(descriptor, config).map(descriptor => descriptor.ref);
        const typeChangedParameters = this.getDescriptorsOfParametersWhereTypeChanged(existingParameters, config.parameterSet.parameters).map(descriptor => descriptor.ref);

        return [...typeChangedParameters, ...missingParameters];
    }

    private getDescriptorsOfMissingParametersInConfig(filterDescriptor: FilterDescriptor, filterConfig: Configuration): ParameterDescriptor[] {
        return partition(filterDescriptor.parameters, (parameter: ParameterDescriptor) =>
            filterConfig.parameterSet.parameters.map(param => param.ref.id).includes(parameter.ref.id))[1] as ParameterDescriptor[];
    }

    private getDescriptorsOfExistingParametersInConfig(filterDescriptor: FilterDescriptor, filterConfig: Configuration): ParameterDescriptor[] {
        return partition(filterDescriptor.parameters, (parameter: ParameterDescriptor) =>
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

    private getDescriptorsOfParametersWhereTypeChanged(existingParameters: ParameterDescriptor[], parameterConfigs: Parameter[]): ParameterDescriptor[] {
        return existingParameters.filter(descriptor => {
            const newParameter: Parameter = this.parameterFactory.parameterDescriptorToParameter(descriptor);
            const existingParameter: Parameter = parameterConfigs.find(param => param.ref.id === descriptor.ref.id);
            return newParameter.jsonClass !== existingParameter.jsonClass;
        });
    }


    constructor(private parameterFactory: ParameterFactoryService) {

    }
}
