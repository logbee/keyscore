import {Action} from "@ngrx/store";
import {Dataset} from "@keyscore-manager-models";

export const EXTRACT_FROM_SELECTED_BLOCK = "[Preview] ExtractFromSelectedBlock";
export const EXTRACT_FROM_SELECTED_BLOCK_SUCCESS = "[Preview] ExtractFromSelectedBlockSuccess";
export const EXTRACT_FROM_SELECTED_BLOCK_FAILURE = "[Preview] ExtractFromSelectedBlockFailure";
export const RESET_PREVIEW_STATE = "[Preview] ResetPreviewState";

export type PreviewActions =
    | ExtractFromSelectedBlock
    | ExtractFromSelectedBlockFailure
    | ExtractFromSelectedBlockSuccess
    | ResetPreviewState;


export class ExtractFromSelectedBlock implements Action {
    public readonly type = EXTRACT_FROM_SELECTED_BLOCK;

    constructor(readonly selectedBlockId: string, readonly where: string, readonly amount:number) {

    }
}

export class ExtractFromSelectedBlockSuccess implements Action {
    public readonly type = EXTRACT_FROM_SELECTED_BLOCK_SUCCESS;

    constructor(readonly extractedDatsets: Dataset[], readonly blockId: string, readonly where: string) {

    }
}

export class ExtractFromSelectedBlockFailure implements Action {
    public readonly type = EXTRACT_FROM_SELECTED_BLOCK_FAILURE;

    constructor(readonly cause: any) {

    }
}

export class ResetPreviewState implements Action {
    public readonly type = RESET_PREVIEW_STATE;
}
