import {PipelineConfigurationChecker} from "@/app/pipelines/services/pipeline-configuration-checker.service";
import {ParameterFactoryService} from "@keyscore-manager-pipeline-parameters/src/main/service/parameter-factory.service";
import {EditingPipelineModel} from "@keyscore-manager-models/src/main/pipeline-model/EditingPipelineModel";
import {BlueprintJsonClass} from "@keyscore-manager-models/src/main/blueprints/Blueprint";
import {
    FilterDescriptor,
    FilterDescriptorJsonClass
} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";
import {
    TextParameter,
    TextParameterDescriptor
} from "@keyscore-manager-models/src/main/parameters/text-parameter.model";
import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";
import * as _ from 'lodash';
import {TestBed} from "@angular/core/testing";
import {TextParameterModule} from "@keyscore-manager-pipeline-parameters/src/main/parameters/text-parameter/text-parameter.module";

describe('PipelineConfigurationChecker', () => {
    let service: PipelineConfigurationChecker, parameterFactory: ParameterFactoryService,
        pipeline: EditingPipelineModel;

    const filterConfig0: Configuration = {
        ref: {uuid: 'filterConfiguration0'},
        parent: null,
        parameterSet: {
            parameters: [
                new TextParameter({id: 'textParameter'}, 'initValue')
            ]
        }
    };

    const editingPipelineModel: EditingPipelineModel = {
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
            filterConfig0
        ]
    };

    const filterDescriptor: FilterDescriptor =
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

    const filterDescriptor2Parameter: FilterDescriptor =
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

    beforeEach(async () => {

        TestBed.configureTestingModule({
            imports: [
                TextParameterModule
            ],
            providers: [
                PipelineConfigurationChecker
            ]
        });

        await TestBed.compileComponents();
        pipeline = _.cloneDeep(editingPipelineModel);
        service = TestBed.get(PipelineConfigurationChecker)


    });

    it('should add a default config for a new parameter ', () => {
        let filterDescriptors: FilterDescriptor[] = [filterDescriptor2Parameter];
        service.alignConfigWithDescriptor(pipeline, filterDescriptors);
        expect(pipeline.configurations.length).toBe(2);
    })
});
