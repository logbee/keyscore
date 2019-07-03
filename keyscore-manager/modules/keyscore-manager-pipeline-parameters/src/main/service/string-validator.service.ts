import {Injectable} from "@angular/core";
import {ExpressionType, ResolvedStringValidator} from "keyscore-manager-models";
import * as GlobMatch from "minimatch";

@Injectable()
export class StringValidatorService {

    public validate(value: string, validator: ResolvedStringValidator): boolean {
        if (!validator) {
            return true;
        }
        switch (validator.expressionType) {
            case ExpressionType.RegEx:
                let regEx: RegExp = new RegExp(validator.expression);
                return regEx.test(value);
            case ExpressionType.Glob:
                return GlobMatch(value, validator.expression);
            default:
                return true;


        }
    }
}