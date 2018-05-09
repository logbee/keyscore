import {FilterDescriptor, FilterModel, ParameterDescriptor} from "../streams/streams.model";
import {createSelector} from "@ngrx/store";

export interface FilterState {
    filter: FilterDescriptor
}


// export const getFilterDescriptorForCurrent = createSelector()