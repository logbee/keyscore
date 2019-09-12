import {Ref} from "@keyscore-manager-models/src/main/common/Ref";
import {MetaData} from "@keyscore-manager-models/src/main/common/MetaData";

export interface PipelineBlueprint {
    ref: Ref;
    blueprints: Ref[];
    metadata: MetaData;
}

export type Blueprint =
    | FilterBlueprint
    | SourceBlueprint
    | SinkBlueprint
    | BranchBlueprint
    | MergeBlueprint;

export enum BlueprintJsonClass {
    FilterBlueprint = "io.logbee.keyscore.model.blueprint.FilterBlueprint",
    SourceBlueprint = "io.logbee.keyscore.model.blueprint.SourceBlueprint",
    SinkBlueprint = "io.logbee.keyscore.model.blueprint.SinkBlueprint",
    BranchBlueprint = "io.logbee.keyscore.model.blueprint.BranchBlueprint",
    MergeBlueprint = "io.logbee.keyscore.model.blueprint.MergeBlueprint"
}

export interface FilterBlueprint {
    readonly jsonClass: BlueprintJsonClass.FilterBlueprint;
    ref: Ref;
    descriptor: Ref;
    configuration: Ref;
    in: Ref;
    out: Ref;
}

export interface SourceBlueprint {
    readonly jsonClass: BlueprintJsonClass.SourceBlueprint;
    ref: Ref;
    descriptor: Ref;
    configuration: Ref;
    out: Ref;
}

export interface SinkBlueprint {
    readonly jsonClass: BlueprintJsonClass.SinkBlueprint;
    ref: Ref;
    descriptor: Ref;
    configuration: Ref;
    in: Ref;
}

export interface BranchBlueprint {
    readonly jsonClass: BlueprintJsonClass.BranchBlueprint;
    ref: Ref;
    descriptor: Ref;
    configuration: Ref;
    in: Ref;
    outA: Ref;
    outB: Ref;
}

export interface MergeBlueprint {
    readonly jsonClass: BlueprintJsonClass.MergeBlueprint;
    ref: Ref;
    descriptor: Ref;
    configuration: Ref;
    out: Ref;
    inA: Ref;
    inB: Ref;
}