import {Ref} from "../common/Ref";
import {MetaData} from "../common/MetaData";

export interface PipelineBlueprint {
    ref: Ref;
    blueprints: Blueprint[];
    metadata: MetaData;
}

export type Blueprint =
    | FilterBlueprint
    | SourceBlueprint
    | SinkBlueprint
    | BranchBlueprint
    | MergeBlueprint;


export interface FilterBlueprint {
    ref: Ref;
    descriptor: Ref;
    configuration: Ref;
    in: Ref;
    out: Ref;
}

export interface SourceBlueprint {
    ref: Ref;
    descriptor: Ref;
    configuration: Ref;
    out: Ref;
}

export interface SinkBlueprint {
    ref: Ref;
    descriptor: Ref;
    configuration: Ref;
    in: Ref;
}

export interface BranchBlueprint {
    ref: Ref;
    descriptor: Ref;
    configuration: Ref;
    in: Ref;
    outA: Ref;
    outB: Ref;
}

export interface MergeBlueprint {
    ref: Ref;
    descriptor: Ref;
    configuration: Ref;
    out: Ref;
    inA: Ref;
    inB: Ref;
}