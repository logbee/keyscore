import {Injectable} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {
    Choice,
    Descriptor,
    FieldNameParameterDescriptor,
    FieldParameterDescriptor,
    FilterDescriptor,
    ParameterDescriptor,
    ParameterInfo,
    ChoiceWithLocales,
    FilterDescriptorWithLocales,
    ParameterDescriptorWithLocales,
    ParameterInfoWithLocales,
    StringValidatorWithLocales,
    StringValidator,
    TextParameterDescriptor,
    ParameterDescriptorJsonClass,
    ExpressionParameter,
    ExpressionParameterDescriptor,
    ExpressionParameterChoice, NumberParameterDescriptor, DecimalParameterDescriptor
} from "@keyscore-manager-models";

@Injectable()
export class DescriptorResolverService {

    constructor(private translateService: TranslateService) {
    }

    resolveDescriptor(descriptor: Descriptor): FilterDescriptor {
        const filterDescriptor: FilterDescriptorWithLocales = descriptor.describes;
        const possibleLanguages = descriptor.localization.locales.map(locale => locale.language);
        const lang = this.translateService.currentLang;
        const currentLang = possibleLanguages.includes(lang) ? lang :
            possibleLanguages.includes('en') ? 'en' : possibleLanguages[0];
        const settings = {descriptor: descriptor, language: currentLang};
        const displayName = filterDescriptor.displayName ?
            this.getTranslation(settings, filterDescriptor.displayName.id) : "";
        const description = filterDescriptor.description ?
            this.getTranslation(settings, filterDescriptor.description.id) : "";
        const categories = filterDescriptor.categories.map(category => {
            return {
                name: category.name,
                displayName: category.displayName ? this.getTranslation(settings, category.displayName.id) : category.name
            }
        });
        const resolvedParameters = filterDescriptor.parameters.map(parameter =>
            this.resolveParameterDescriptor(settings, parameter));

        let resolvedDescriptor = {
            descriptorRef: descriptor.ref,
            name: filterDescriptor.name,
            jsonClass: filterDescriptor.jsonClass,
            displayName: displayName,
            description: description,
            categories: categories,
            parameters: resolvedParameters
        };

        if (descriptor.describes.icon) {
            return {
                ...resolvedDescriptor,
                icon: descriptor.describes.icon
            };
        }
        else {
            return resolvedDescriptor;

        }
    }


    private resolveParameterDescriptor(settings: { descriptor: Descriptor, language: string }, parameterDescriptor: ParameterDescriptorWithLocales): ParameterDescriptor {
        let base = {
            ref: parameterDescriptor.ref,
            info: this.resolveInfo(settings, parameterDescriptor.info),
            jsonClass: parameterDescriptor.jsonClass,
        };
        switch (parameterDescriptor.jsonClass) {
            case ParameterDescriptorJsonClass.TextParameterDescriptor: {
                return new TextParameterDescriptor(
                    base.ref,
                    base.info.displayName,
                    base.info.description,
                    parameterDescriptor.defaultValue,
                    this.resolveValidator(settings, parameterDescriptor.validator),
                    parameterDescriptor.mandatory);
            }
            case ParameterDescriptorJsonClass.ExpressionParameterDescriptor:{
                const choices = parameterDescriptor.choices.map(choice => this.resolveChoice(settings, choice)).map(choice => new ExpressionParameterChoice(choice.name, choice.displayName, choice.description));
                return new ExpressionParameterDescriptor(base.ref, base.info.displayName, base.info.description, parameterDescriptor.defaultValue, parameterDescriptor.mandatory, choices);
            }
            case ParameterDescriptorJsonClass.NumberParameterDescriptor:
                return new NumberParameterDescriptor(base.ref,base.info.displayName,base.info.description,parameterDescriptor.defaultValue,parameterDescriptor.range,parameterDescriptor.mandatory);
            case ParameterDescriptorJsonClass.DecimalParameterDescriptor:
                return new DecimalParameterDescriptor(base.ref,base.info.displayName,base.info.description,parameterDescriptor.defaultValue,parameterDescriptor.range,parameterDescriptor.decimals,parameterDescriptor.mandatory);
            case ParameterDescriptorJsonClass.FieldNameParameterDescriptor: {
                const validator = this.resolveValidator(settings, parameterDescriptor.validator);
                return new FieldNameParameterDescriptor(base.ref, base.info.displayName, base.info.description, parameterDescriptor.defaultValue, parameterDescriptor.hint, validator, parameterDescriptor.mandatory);
            }
            case ParameterDescriptorJsonClass.FieldParameterDescriptor: {
                const validator = this.resolveValidator(settings, parameterDescriptor.validator);
                return new FieldParameterDescriptor(base.ref, base.info.displayName, base.info.description, parameterDescriptor.defaultValue, parameterDescriptor.hint, validator,parameterDescriptor.fieldValueType,parameterDescriptor.mandatory);
            }
            case ParameterDescriptorJsonClass.TextListParameterDescriptor:
                return {
                    ...initialize,
                    descriptor: parameterDescriptor.descriptor ?
                        this.resolveParameterDescriptor(settings, parameterDescriptor.descriptor) as TextParameterDescriptor : null,
                    min: parameterDescriptor.min,
                    max: parameterDescriptor.max,

                };
            case ParameterDescriptorJsonClass.FieldNameListParameterDescriptor:
                return {
                    ...initialize,
                    descriptor: parameterDescriptor.descriptor ? this.resolveParameterDescriptor(settings, parameterDescriptor.descriptor) as FieldNameParameterDescriptor : null,
                    min: parameterDescriptor.min,
                    max: parameterDescriptor.max,

                };
            case ParameterDescriptorJsonClass.FieldListParameterDescriptor:
                return {
                    ...initialize,
                    descriptor: parameterDescriptor.descriptor ? this.resolveParameterDescriptor(settings, parameterDescriptor.descriptor) as FieldParameterDescriptor : null,
                    min: parameterDescriptor.min,
                    max: parameterDescriptor.max,

                };
            case ParameterDescriptorJsonClass.ChoiceParameterDescriptor:
                return {
                    ...initialize,
                    min: parameterDescriptor.min,
                    max: parameterDescriptor.max,
                    choices: parameterDescriptor.choices.map(choice => this.resolveChoice(settings, choice))
                };
            case ParameterDescriptorJsonClass.BooleanParameterDescriptor:
                return {
                    ...initialize,
                    mandatory: parameterDescriptor.mandatory,
                    defaultValue: parameterDescriptor.defaultValue
                };
            case ParameterDescriptorJsonClass.FieldNamePatternParameterDescriptor:
                return {
                    ...initialize,
                    defaultValue: parameterDescriptor.defaultValue,
                    hint: parameterDescriptor.hint,
                    supports: parameterDescriptor.supports,
                    mandatory: parameterDescriptor.mandatory
                };

            default:
                return null;


        }

    }

    private resolveInfo(settings: { descriptor: Descriptor, language: string }, info: ParameterInfoWithLocales): ParameterInfo {
        return info ? {
            displayName: info.displayName ? this.getTranslation(settings, info.displayName.id) : "",
            description: info.description ? this.getTranslation(settings, info.description.id) : ""
        } : {displayName: "", description: ""};
    }

    /*private resolveDirectiveDescriptor(settings: { descriptor: Descriptor, language: string }, directive: FieldDirectiveDescriptorWithLocales): FieldDirectiveDescriptor {
        return directive ? {
            ...directive,
            info: this.resolveInfo(settings, directive.info),
            parameters: directive.parameters.map(parameter => this.resolveParameterDescriptor(settings, parameter))
        } : null;
    }*/

    private resolveChoice(settings: { descriptor: Descriptor, language: string }, choice: ChoiceWithLocales): Choice {
        return choice ? {
            ...choice,
            displayName: choice.displayName ? this.getTranslation(settings, choice.displayName.id) : "",
            description: choice.description ? this.getTranslation(settings, choice.description.id) : ""
        } : null;
    }

    private resolveValidator(settings: { descriptor: Descriptor, language: string }, validator: StringValidatorWithLocales): StringValidator {
        return validator ? {
            ...validator,
            description: validator.description ? this.getTranslation(settings, validator.description.id) : ""
        } : null;
    }

    private getTranslation(settings: { descriptor: Descriptor, language: string }, key: string) {
        return settings.descriptor.localization.mapping[key] ?
            settings.descriptor.localization.mapping[key].translations[settings.language] : "";
    }
}
