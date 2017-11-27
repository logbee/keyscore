import { Action } from '@ngrx/store';

export class Stream {
    name: String;
    description: String;
    filters: FilterInstance[];
}

export class FilterInstance {
    constructor(public id: String,
                public name: String,
                public description: String = '',
                public editing: boolean = false,
                public enabled: boolean = true) {}
}

export const initialState: Stream = {
    name: 'Test Stream',
    description: 'This is a test stream.',
    filters: [
      new FilterInstance('1', 'Kafka Input', 'Where does it come from?'),
      new FilterInstance('2', 'Drop crap', 'Drop the most'),
      new FilterInstance('3', 'Add fields', 'There are very interesting fields!'),
      new FilterInstance('4', 'Kafka Output', ''),
    ]
};

const REMOVE_FILTER = 'REMOVE_FILTER';
const MOVE_FILTER = 'MOVE_FILTER';
const EDIT_FILTER = 'EDIT_FILTER';
const SAVE_FILTER = 'SAVE_FILTER';
const ENABLE_FILTER = 'ENABLE_FILTER';
const DISABLE_FILTER = 'DISABLE_FILTER';

export class RemoveFilterAction implements Action {
    readonly type = REMOVE_FILTER;
    constructor(public filterId: String) {}
}

export class MoveFilterAction implements Action {
    readonly type = MOVE_FILTER;
    constructor(public filterId: String, public position: number) {}
}

export class EditFilterAction implements Action {
    readonly type = EDIT_FILTER;
    constructor(public filterId: String) {}
}

export class SaveFilterAction implements Action {
    readonly type = SAVE_FILTER;
    constructor(public filterId: String) {}
}

export class EnableFilterAction implements Action {
    readonly type = ENABLE_FILTER;
    constructor(public filterId: String) {}
}

export class DisableFilterAction implements Action {
    readonly type = DISABLE_FILTER;
    constructor(public filterId: String) {}
}

export function streamReducer(state: Stream = initialState, action: Action): Stream {

    const result: Stream = Object.assign({}, state);

    switch (action.type) {
        case REMOVE_FILTER:
            const removeFilterAction = action as RemoveFilterAction;
            result.filters = state.filters.filter(filter => filter.id != removeFilterAction.filterId);
            return result;
        case MOVE_FILTER:
            const moveFilterAction = action as MoveFilterAction;
            const index = state.filters.findIndex(filter => filter.id == moveFilterAction.filterId);
            swap(result.filters, index, moveFilterAction.position);
            return result;
        case EDIT_FILTER:
            const editFilterAction = action as EditFilterAction;
            result.filters.find(filter => filter.id == editFilterAction.filterId).editing = true;
            return result;
        case SAVE_FILTER:
            const saveFilterAction = action as SaveFilterAction;
            result.filters.find(filter => filter.id == saveFilterAction.filterId).editing = false;
            return result;
        case ENABLE_FILTER:
            const enableFilterAction = action as EnableFilterAction;
            result.filters.find(filter => filter.id == enableFilterAction.filterId).enabled = true;
            return result;
        case DISABLE_FILTER:
            const disableFilterAction = action as DisableFilterAction;
            result.filters.find(filter => filter.id == disableFilterAction.filterId).enabled = false;
            return result;
        default:
            return result;
    }
}

function swap<T>(arr: Array<T>, a: number, b: number) {
    if (a >= 0 && a < arr.length && b >= 0 && b < arr.length) {
        const temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }
}