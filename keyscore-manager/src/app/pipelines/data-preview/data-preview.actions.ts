import {Action} from "@ngrx/store";

export const TRIGGER_DATA_PREVIEW = "[DataPreview] TriggerDataPreview";

export type DataPreviewActions =
    TriggerDataPreview;

export class TriggerDataPreview implements Action {
    public readonly type = TRIGGER_DATA_PREVIEW;
}