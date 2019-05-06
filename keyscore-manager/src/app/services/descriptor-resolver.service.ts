import {Injectable} from "@angular/core";
import {TranslateService} from "@ngx-translate/core";
import {
    Choice,
    Descriptor,
    FieldDirectiveDescriptor,
    FieldNameParameterDescriptor,
    FieldParameterDescriptor,
    FilterDescriptor,
    ParameterDescriptor,
    ParameterDescriptorJsonClass,
    ParameterInfo,
    ResolvedChoice,
    ResolvedFieldDirectiveDescriptor,
    ResolvedFilterDescriptor,
    ResolvedParameterDescriptor,
    ResolvedParameterInfo,
    ResolvedStringValidator,
    StringValidator,
    TextParameterDescriptor
} from "keyscore-manager-models";

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
        const displayName = filterDescriptor.displayName ?
            this.getTranslation(settings, filterDescriptor.displayName.id) : "N/A";
        const description = filterDescriptor.description ?
            this.getTranslation(settings, filterDescriptor.description.id) : "N/A";
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


    private resolveParameterDescriptor(settings: { descriptor: Descriptor, language: string }, parameterDescriptor: ParameterDescriptor): ResolvedParameterDescriptor {
        let initialize = {
            ref: parameterDescriptor.ref,
            info: this.resolveInfo(settings, parameterDescriptor.info),
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
            case ParameterDescriptorJsonClass.FieldDirectiveSequenceParameterDescriptor:
                return {
                    ...initialize,
                    fieldTypes: parameterDescriptor.fieldTypes,
                    parameters: parameterDescriptor.parameters.map(parameter => this.resolveParameterDescriptor(settings, parameter)),
                    directives: parameterDescriptor.directives.map(directive => this.resolveDirectiveDescriptor(settings, directive)),
                    minSequences: parameterDescriptor.minSequences,
                    maxSequences: parameterDescriptor.maxSequences
                };
            default:
                return null;


        }

    }

    private resolveInfo(settings: { descriptor: Descriptor, language: string }, info: ParameterInfo): ResolvedParameterInfo {
        return info ? {
            displayName: info.displayName ? this.getTranslation(settings, info.displayName.id) : "",
            description: info.description ? this.getTranslation(settings, info.description.id) : ""
        } : {displayName: "", description: ""};
    }

    private resolveDirectiveDescriptor(settings: { descriptor: Descriptor, language: string }, directive: FieldDirectiveDescriptor): ResolvedFieldDirectiveDescriptor {
        return directive ? {
            ...directive,
            info: this.resolveInfo(settings, directive.info),
            parameters: directive.parameters.map(parameter => this.resolveParameterDescriptor(settings, parameter))
        } : null;
    }

    private resolveChoice(settings: { descriptor: Descriptor, language: string }, choice: Choice): ResolvedChoice {
        return choice ? {
            ...choice,
            displayName: choice.displayName ? this.getTranslation(settings, choice.displayName.id) : "N/A",
            description: choice.description ? this.getTranslation(settings, choice.description.id) : "N/A"
        } : null;
    }

    private resolveValidator(settings: { descriptor: Descriptor, language: string }, validator: StringValidator): ResolvedStringValidator {
        return validator ? {
            ...validator,
            description: validator.description ? this.getTranslation(settings, validator.description.id) : "N/A"
        } : null;
    }

    private getTranslation(settings: { descriptor: Descriptor, language: string }, key: string) {
        return settings.descriptor.localization.mapping[key] ?
            settings.descriptor.localization.mapping[key].translations[settings.language] : "N/A";
    }
}
