import {Injectable} from "@angular/core";
import {Configuration} from "@keyscore-manager-models/src/main/common/Configuration";
import {Parameter} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {JSONCLASS_TEXT_PARAM, TextParameter} from "@keyscore-manager-models/src/main/parameters/text-parameter.model";
import {
    JSONCLASS_NUMBER_PARAM,
    NumberParameter
} from "@keyscore-manager-models/src/main/parameters/number-parameter.model";
import {
    FieldNameParameter,
    JSONCLASS_FIELDNAME_PARAM
} from "@keyscore-manager-models/src/main/parameters/field-name-parameter.model";
import {
    BooleanParameter,
    JSONCLASS_BOOLEAN_PARAM
} from "@keyscore-manager-models/src/main/parameters/boolean-parameter.model";
import {
    JSONCLASS_PASSWORD_PARAM,
    PasswordParameter
} from "@keyscore-manager-models/src/main/parameters/password-parameter.model";
import {
    ChoiceParameter,
    JSONCLASS_CHOICE_PARAM
} from "@keyscore-manager-models/src/main/parameters/choice-parameter.model";
import {
    DecimalParameter,
    JSONCLASS_DECIMAL_PARAM
} from "@keyscore-manager-models/src/main/parameters/decimal-parameter.model";
import {
    ExpressionParameter,
    JSONCLASS_EXPRESSION_PARAM
} from "@keyscore-manager-models/src/main/parameters/expression-parameter.model";
import {
    FieldNamePatternParameter,
    JSONCLASS_FIELDNAMEPATTERN_PARAM
} from "@keyscore-manager-models/src/main/parameters/field-name-pattern-parameter.model";
import {
    FieldParameter,
    JSONCLASS_FIELD_PARAM
} from "@keyscore-manager-models/src/main/parameters/field-parameter.model";
import {
    FieldListParameter,
    JSONCLASS_FIELDLIST_PARAM
} from "@keyscore-manager-models/src/main/parameters/parameter-lists/field-list-parameter.model";
import {
    FieldNameListParameter,
    JSONCLASS_FIELDNAMELIST_PARAM
} from "@keyscore-manager-models/src/main/parameters/parameter-lists/field-name-list-parameter.model";
import {
    JSONCLASS_TEXTLIST_PARAM,
    TextListParameter
} from "@keyscore-manager-models/src/main/parameters/parameter-lists/text-list-parameter.model";
import {
    JSONCLASS_GROUP_PARAM,
    ParameterGroup
} from "@keyscore-manager-models/src/main/parameters/group-parameter.model";
import {Field} from "@keyscore-manager-models/src/main/dataset/Field";
import {
    FieldDirectiveSequenceConfiguration,
    FieldDirectiveSequenceParameter,
    JSONCLASS_DIRECTIVE_SEQ_PARAM
} from "@keyscore-manager-models/src/main/parameters/directive.model";
import {cloneDeep} from 'lodash-es';

@Injectable({providedIn: 'root'})
export class ConfigDeserializer {

    public deserializeConfig(config: Configuration): Configuration {
        let result = cloneDeep(config);
        result.parameterSet.parameters = config.parameterSet.parameters.map(parameter => this.deserializeParameter(parameter));
        return result;
    }

    private deserializeParameter(parameter: Parameter): Parameter {

        switch (parameter.jsonClass) {
            case JSONCLASS_TEXT_PARAM:
                return new TextParameter(parameter.ref, parameter.value);
            case JSONCLASS_NUMBER_PARAM:
                return new NumberParameter(parameter.ref, parameter.value);
            case JSONCLASS_FIELDNAME_PARAM:
                return new FieldNameParameter(parameter.ref, parameter.value);
            case JSONCLASS_BOOLEAN_PARAM:
                return new BooleanParameter(parameter.ref, parameter.value);
            case JSONCLASS_PASSWORD_PARAM:
                return new PasswordParameter(parameter.ref, parameter.value);
            case JSONCLASS_CHOICE_PARAM:
                return new ChoiceParameter(parameter.ref, parameter.value);
            case JSONCLASS_DECIMAL_PARAM:
                return new DecimalParameter(parameter.ref, parameter.value);
            case JSONCLASS_EXPRESSION_PARAM:
                return new ExpressionParameter(parameter.ref, parameter.value, parameter.choice);
            case JSONCLASS_FIELDNAMEPATTERN_PARAM:
                return new FieldNamePatternParameter(parameter.ref, parameter.value, parameter.patternType);
            case JSONCLASS_FIELD_PARAM:
                return new FieldParameter(parameter.ref, this.deserializeField(parameter.value));
            case JSONCLASS_FIELDLIST_PARAM:
                const fields: Field[] = parameter.value.map(field => this.deserializeField(field));
                return new FieldListParameter(parameter.ref, fields);
            case JSONCLASS_FIELDNAMELIST_PARAM:
                return new FieldNameListParameter(parameter.ref, parameter.value);
            case JSONCLASS_TEXTLIST_PARAM:
                return new TextListParameter(parameter.ref, parameter.value);
            case JSONCLASS_GROUP_PARAM:
                const parameters = parameter.value.parameters.map(parameter => this.deserializeParameter(parameter));
                return new ParameterGroup(parameter.ref, {parameters: parameters});
            case JSONCLASS_DIRECTIVE_SEQ_PARAM:
                const directiveSequenceConfig: FieldDirectiveSequenceConfiguration[] = parameter.value.map(sequence =>
                    this.deserializeFieldDirectiveSequenceConfiguration(sequence));
                return new FieldDirectiveSequenceParameter(parameter.ref, directiveSequenceConfig);
        }
        this.exhaustiveCheck(parameter);


    }

    private exhaustiveCheck(param: never): never {
        throw new Error(`[ConfigDeserializer] Can't deserialize unknown parameter: ${param}.`);
    }

    private deserializeField(fieldJson: any): Field {
        if (!fieldJson.hasOwnProperty('name')) {
            throw new Error(`[ConfigDeserializerService] Can't deserialize ${fieldJson} to Field because property 'name' is missing `);
        }
        if (!fieldJson.hasOwnProperty('value')) {
            throw new Error(`[ConfigDeserializerService] Cannot deserialize ${fieldJson} to Field because property 'value' is missing `);
        }
        return new Field(fieldJson.name, fieldJson.value);
    }

    private deserializeFieldDirectiveSequenceConfiguration(configJson: FieldDirectiveSequenceConfiguration): FieldDirectiveSequenceConfiguration {
        let result = cloneDeep(configJson);
        let sequenceParameter: Parameter[] = configJson.parameters.parameters.map(parameter => this.deserializeParameter(parameter));
        result.parameters.parameters = sequenceParameter;

        result.directives.forEach(directive => {
            let directiveParameter: Parameter[] = directive.parameters.parameters.map(parameter => this.deserializeParameter(parameter));
            directive.parameters.parameters = directiveParameter;
        });

        return result;

    }

}
