import {Action} from "@ngrx/store";

export const TEST_ACTION = "[Preview] Testaction";

export type PreviewActions =
    | TestAction;

export class TestAction implements Action {
    public readonly type = TEST_ACTION;
}