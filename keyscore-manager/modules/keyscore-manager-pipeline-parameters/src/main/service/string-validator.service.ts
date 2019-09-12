import {Injectable} from "@angular/core";
import * as GlobMatch from "minimatch";
import {StringValidator, ExpressionType} from "@/../modules/keyscore-manager-models/src/main/parameters/parameter-fields.model";

@Injectable()
export class StringValidatorService {

    public validate(value: string, validator: StringValidator): boolean {
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