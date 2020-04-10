import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";
import {
  TextParameter,
  TextParameterDescriptor
} from "@keyscore-manager-models/src/main/parameters/text-parameter.model";
import {
  NumberParameter,
  NumberParameterDescriptor
} from "@keyscore-manager-models/src/main/parameters/number-parameter.model";
import {EditingPipelineModel} from "@keyscore-manager-models/src/main/pipeline-model/EditingPipelineModel";
import {BlueprintJsonClass} from "@keyscore-manager-models/src/main/blueprints/Blueprint";
import {
  FilterDescriptor,
  FilterDescriptorJsonClass
} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";

export const filterConfig1Parameter: Configuration = {
  ref: {uuid: 'filterConfiguration0'},
  parent: null,
  parameterSet: {
    parameters: [
      new TextParameter({id: 'textParameter'}, 'initValue')
    ]
  }
};

export const filterConfig2Parameter: Configuration = {
  ...filterConfig1Parameter,
  parameterSet: {
    parameters: [
      new TextParameter({id: 'textParameter'}, 'initValue'),
      new TextParameter({id: 'textParameter2'}, 'defaultValue')
    ]
  }
};

export const filterConfig2ParameterTypeChanged: Configuration = {
  ...filterConfig1Parameter,
  parameterSet: {
    parameters: [
      new TextParameter({id: 'textParameter'}, 'initValue'),
      new NumberParameter({id: 'textParameter2'}, 5)
    ]
  }
};

export const editingPipelineModel: EditingPipelineModel = {
  pipelineBlueprint: {
    ref: {
      uuid: 'testPipeline'
    },
    blueprints: [
      {uuid: 'filterBlueprint0'}
    ],
    metadata: null
  },
  blueprints: [
    {
      jsonClass: BlueprintJsonClass.FilterBlueprint,
      ref: {uuid: 'filterBlueprint0'},
      descriptor: {uuid: 'filterDescriptor0'},
      configuration: {uuid: 'filterConfiguration0'},
      in: null,
      out: null
    }
  ],
  configurations: [
    filterConfig1Parameter
  ]
};

export const editingPipelineModel2Configs: EditingPipelineModel = {
  ...editingPipelineModel,
  configurations: [
    filterConfig2Parameter
  ]
};

export const filterDescriptor: FilterDescriptor =
  {
    jsonClass: FilterDescriptorJsonClass.FilterDescriptor,
    descriptorRef: {uuid: 'filterDescriptor0'},
    name: 'testFilter',
    displayName: '',
    description: '',
    categories: [],
    parameters: [
      new TextParameterDescriptor({id: 'textParameter'}, '', '', 'defaultValue', null, false)
    ]
  };

export const filterDescriptor2Parameter: FilterDescriptor =
  {
    jsonClass: FilterDescriptorJsonClass.FilterDescriptor,
    descriptorRef: {uuid: 'filterDescriptor0'},
    name: 'testFilter',
    displayName: '',
    description: '',
    categories: [],
    parameters: [
      new TextParameterDescriptor({id: 'textParameter'}, '', '', 'defaultValue', null, false),
      new TextParameterDescriptor({id: 'textParameter2'}, '', '', 'defaultValue', null, false)

    ]
  };

export const filterDescriptors2ParameterTypeChanged: FilterDescriptor = {
  ...filterDescriptor2Parameter,
  parameters: [
    new TextParameterDescriptor({id: 'textParameter'}, '', '', 'defaultValue', null, false),
    new NumberParameterDescriptor({id: 'textParameter2'}, '', '', 5, null, false)
  ]
}
