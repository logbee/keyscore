import {Injectable} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {Descriptor} from "../models/descriptors/Descriptor";
import {FilterDescriptor, ResolvedFilterDescriptor} from "../models/descriptors/FilterDescriptor";
import {
    Choice, FieldNameParameterDescriptor, FieldParameterDescriptor,
    ParameterDescriptor, ParameterDescriptorJsonClass, ResolvedChoice, ResolvedParameterDescriptor,
    ResolvedParameterInfo, ResolvedStringValidator, StringValidator, TextParameterDescriptor
} from "../models/parameters/ParameterDescriptor";

@Injectable()
export class DescriptorResolverService {

    constructor(private translateService: TranslateService) {
    }

    resolveDescriptor(descriptor: Descriptor): ResolvedFilterDescriptor {
        const filterDescriptor: FilterDescriptor = descriptor.describes;
        const possibleLanguages = descriptor.localization.locales.map(locale => locale.language);
        const lang = this.translateService.currentLang;
        const currentLang = possibleLanguages.includes(lang) ? lang :
            possibleLanguages.includes('en') ? 'en' : possibleLanguages[0];
        const settings = {descriptor: descriptor, language: currentLang};
        const displayName = this.getTranslation(settings, filterDescriptor.displayName.id);
        const description = this.getTranslation(settings, filterDescriptor.description.id);
        const categories = filterDescriptor.categories.map(category => {
            return {
                name: category.name,
                displayName: this.getTranslation(settings, category.displayName.id)
            }
        });
        const resolvedParameters = filterDescriptor.parameters.map(parameter =>
            this.resolveParameterDescriptor(settings, parameter));


        return {
            descriptorRef: descriptor.ref,
            name: filterDescriptor.name,
            jsonClass: filterDescriptor.jsonClass,
            displayName: displayName,
            description: description,
            categories: categories,
            parameters: resolvedParameters
        };
    }

    private resolveParameterDescriptor(settings: { descriptor: Descriptor, language: string }, parameterDescriptor: ParameterDescriptor): ResolvedParameterDescriptor {
        const resolvedInfo: ResolvedParameterInfo = parameterDescriptor.info ? {
            displayName: this.getTranslation(settings, parameterDescriptor.info.displayName.id),
            description: this.getTranslation(settings, parameterDescriptor.info.description.id)
        } : {displayName: "", description: ""};

        let initialize = {
            ref: parameterDescriptor.ref,
            info: resolvedInfo,
            jsonClass: parameterDescriptor.jsonClass,
        };
        switch (parameterDescriptor.jsonClass) {
            case ParameterDescriptorJsonClass.TextParameterDescriptor:
                return {
                    ...initialize,
                    defaultValue: parameterDescriptor.defaultValue,
                    validator: this.resolveValidator(settings, parameterDescriptor.validator),
                    mandatory: parameterDescriptor.mandatory
                };
            case ParameterDescriptorJsonClass.ExpressionParameterDescriptor:
                return {
                    ...initialize,
                    defaultValue: parameterDescriptor.defaultValue,
                    expressionType: parameterDescriptor.expressionType,
                    mandatory: parameterDescriptor.mandatory
                };
            case ParameterDescriptorJsonClass.NumberParameterDescriptor:
                return {
                    ...initialize,
                    defaultValue: parameterDescriptor.defaultValue,
                    range: parameterDescriptor.range,
                    mandatory: parameterDescriptor.mandatory
                };
            case ParameterDescriptorJsonClass.DecimalParameterDescriptor:
                return {
                    ...initialize,
                    defaultValue: parameterDescriptor.defaultValue,
                    range: parameterDescriptor.range,
                    decimals: parameterDescriptor.decimals,
                    mandatory: parameterDescriptor.mandatory
                };
            case ParameterDescriptorJsonClass.FieldNameParameterDescriptor:
                return {
                    ...initialize,
                    defaultValue: parameterDescriptor.defaultValue,
                    hint: parameterDescriptor.hint,
                    validator: this.resolveValidator(settings, parameterDescriptor.validator),
                    mandatory: parameterDescriptor.mandatory
                };
            case ParameterDescriptorJsonClass.FieldParameterDescriptor:
                return {
                    ...initialize,
                    defaultValue: parameterDescriptor.defaultValue,
                    hint: parameterDescriptor.hint,
                    nameValidator: this.resolveValidator(settings, parameterDescriptor.nameValidator),
                    fieldValueType: parameterDescriptor.fieldValueType,
                    mandatory: parameterDescriptor.mandatory
                };
            case ParameterDescriptorJsonClass.TextListParameterDescriptor:
                return {
                    ...initialize,
                    descriptor: this.resolveParameterDescriptor(settings, parameterDescriptor.descriptor) as TextParameterDescriptor,
                    min: parameterDescriptor.min,
                    max: parameterDescriptor.max,

                };
            case ParameterDescriptorJsonClass.FieldNameListParameterDescriptor:
                return {
                    ...initialize,
                    descriptor: this.resolveParameterDescriptor(settings, parameterDescriptor.descriptor) as FieldNameParameterDescriptor,
                    min: parameterDescriptor.min,
                    max: parameterDescriptor.max,

                };
            case ParameterDescriptorJsonClass.FieldListParameterDescriptor:
                return {
                    ...initialize,
                    descriptor: this.resolveParameterDescriptor(settings, parameterDescriptor.descriptor) as FieldParameterDescriptor,
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
            default:
                return null;


        }

    }

    private resolveChoice(settings: { descriptor: Descriptor, language: string }, choice: Choice): ResolvedChoice {
        return choice ? {
            ...choice,
            displayName: this.getTranslation(settings, choice.displayName.id),
            description: this.getTranslation(settings, choice.description.id)
        } : null;
    }

    private resolveValidator(settings: { descriptor: Descriptor, language: string }, validator: StringValidator): ResolvedStringValidator {
        return validator ? {
            ...validator,
            description: this.getTranslation(settings, validator.description.id)
        } : null;
    }

    private getTranslation(settings: { descriptor: Descriptor, language: string }, key: string) {
        return settings.descriptor.localization.mapping[key].translations[settings.language];
    }
}
