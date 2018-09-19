import {initialState, PipelinesReducer} from "../../app/pipelines/pipelines.reducer"
import {
    LoadFilterDescriptorsSuccessAction,
    ResolveFilterDescriptorSuccessAction
} from "../../app/pipelines/pipelines.actions";
import {removeFieldFilterDescriptorJson, resolvedRemoveFieldsFilterDE} from "../fake-data/descriptor-resolver-fakes";

describe('Pipelines Reducer', () => {
    describe('undefined action', () => {
        it('should return the default state', () => {
            const action = {type: 'NOOP'} as any;
            const result = PipelinesReducer(undefined, action);

            expect(result).toEqual(initialState);
        });
    });
    describe('[Pipeline] ResolveFilterDescriptorSuccess', () => {
        it('should update the categories and resolved descriptors', () => {
            const action = new ResolveFilterDescriptorSuccessAction([resolvedRemoveFieldsFilterDE]);
            const result = PipelinesReducer(initialState, action);
            const categories = [{
                name: "contrib.remove-drop",
                displayName: "Entfernen/Verwerfen"
            }];

            expect(result).toEqual({
                ...initialState,
                filterDescriptors: [resolvedRemoveFieldsFilterDE],
                filterCategories: categories
            });
        });
    });
    describe('[Pipeline] LoadFilterDescriptorsSuccess',() =>{
        it('should update the descriptors',() =>{
            const descriptor = JSON.parse(removeFieldFilterDescriptorJson);
            const action = new LoadFilterDescriptorsSuccessAction([descriptor]);
            const result = PipelinesReducer(initialState,action);

            expect(result).toEqual({
                ...initialState,
                descriptors: [descriptor]
            });
        });
    });
});