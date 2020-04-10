import {EditingPipelineModel} from "@keyscore-manager-models/src/main/pipeline-model/EditingPipelineModel";
import {TestBed} from "@angular/core/testing";

import {PipelineConfigurationChecker} from "@/app/pipelines/services/pipeline-configuration-checker.service";
import {TextParameterModule} from "@keyscore-manager-pipeline-parameters/src/main/parameters/text-parameter/text-parameter.module";
import {FilterDescriptor} from "@keyscore-manager-models/src/main/descriptors/FilterDescriptor";
import {NumberParameterModule} from "@keyscore-manager-pipeline-parameters/src/main/parameters/number-parameter/number-parameter.module";
import {ParameterRef} from "@keyscore-manager-models/src/main/common/Ref";
import {cold} from "jasmine-marbles";
import {
  editingPipelineModel, editingPipelineModel2Configs, filterConfig2Parameter, filterConfig2ParameterTypeChanged,
  filterDescriptor, filterDescriptor2Parameter, filterDescriptors2ParameterTypeChanged
} from "@keyscore-manager-test-fixtures/src/main/pipelines/pipeline-configurations.fixtures";

describe('PipelineConfigurationChecker', () => {
  let service: PipelineConfigurationChecker;


  beforeAll(async () => {

    TestBed.configureTestingModule({
      imports: [
        TextParameterModule,
        NumberParameterModule
      ],
      providers: [
        PipelineConfigurationChecker
      ]
    });

    await TestBed.compileComponents();
    service = TestBed.get(PipelineConfigurationChecker)


  });

  describe("#alignConfigWithDescriptor", () => {
    it('should return null if no pipeline is specified', () => {
      const result = service.alignConfigWithDescriptor(undefined, []);
      expect(result).toBeNull();
    });

    it('should do nothing if the filterDescriptors are undefined or empty', () => {
      let result = service.alignConfigWithDescriptor(editingPipelineModel, undefined);
      expect(result).toEqual(editingPipelineModel);

      result = service.alignConfigWithDescriptor(editingPipelineModel, []);
      expect(result).toEqual(editingPipelineModel);
    });

    it('should do nothing if the descriptor matches the config', () => {
      let filterDescriptors: FilterDescriptor[] = [filterDescriptor];
      const result: EditingPipelineModel = service.alignConfigWithDescriptor(editingPipelineModel, filterDescriptors);
      expect(result).toEqual(editingPipelineModel);
    });

    it('should add a default config for a new parameter ', () => {
      let filterDescriptors: FilterDescriptor[] = [filterDescriptor2Parameter];
      const result: EditingPipelineModel = service.alignConfigWithDescriptor(editingPipelineModel, filterDescriptors);
      expect(result.configurations[0]).toEqual(filterConfig2Parameter);
    });

    it('should add a default config if the parameter type changed', () => {
      let filterDescriptors: FilterDescriptor[] = [filterDescriptors2ParameterTypeChanged];
      const result: EditingPipelineModel = service.alignConfigWithDescriptor(editingPipelineModel, filterDescriptors);
      expect(result.configurations[0]).toEqual(filterConfig2ParameterTypeChanged);
    });

    it('should remove unnecessary configurations', () => {
      let filterDescriptors: FilterDescriptor[] = [filterDescriptor];
      const result: EditingPipelineModel = service.alignConfigWithDescriptor(editingPipelineModel2Configs, filterDescriptors);
      expect(result).toEqual(editingPipelineModel);
    });

  });
  describe('#changedParameters$', () => {
    it('should emit a map with empty ref lists if nothing got updated', () => {
      let filterDescriptors: FilterDescriptor[] = [filterDescriptor];
      service.alignConfigWithDescriptor(editingPipelineModel, filterDescriptors);

      const expectedMap: Map<string, ParameterRef[]> = new Map([['filterBlueprint0', []]]);
      const expected = cold('a', {a: expectedMap});

      expect(service.changedParameters$).toBeObservable(expected);
    });

    it('should emit a map with an empty ref list for removed configs ', () => {
      let filterDescriptors: FilterDescriptor[] = [filterDescriptor];
      service.alignConfigWithDescriptor(editingPipelineModel2Configs, filterDescriptors);

      const expectedMap: Map<string, ParameterRef[]> = new Map([['filterBlueprint0', []]]);
      const expected = cold('a', {a: expectedMap});

      expect(service.changedParameters$).toBeObservable(expected);
    });

    it('should emit a map with the ref of the added parameter', () => {
      let filterDescriptors: FilterDescriptor[] = [filterDescriptor2Parameter];
      service.alignConfigWithDescriptor(editingPipelineModel, filterDescriptors);

      const expectedMap: Map<string, ParameterRef[]> = new Map([['filterBlueprint0', [{id: 'textParameter2'}]]]);
      const expected = cold('a', {a: expectedMap});

      expect(service.changedParameters$).toBeObservable(expected);
    });

    it('should emit a map with the ref of the type changed parameter', () => {
      let filterDescriptors: FilterDescriptor[] = [filterDescriptors2ParameterTypeChanged];
      service.alignConfigWithDescriptor(editingPipelineModel, filterDescriptors);

      const expectedMap: Map<string, ParameterRef[]> = new Map([['filterBlueprint0', [{id: 'textParameter2'}]]]);
      const expected = cold('a', {a: expectedMap});

      expect(service.changedParameters$).toBeObservable(expected);
    });


  })
});







