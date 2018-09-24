import {Parameter} from "../parameters/Parameter";
import {Ref} from "./Ref";

export interface Configuration {
    ref: Ref;
    parent:Ref;
    parameters: Parameter[];
}
