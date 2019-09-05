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
    ExpressionParameterDescriptor,
    ExpressionParameterChoice,
    NumberParameterDescriptor,
    DecimalParameterDescriptor,
    TextListParameterDescriptor,
    FieldNameListParameterDescriptor,
    FieldListParameterDescriptor,
    ChoiceParameterDescriptor,
    BooleanParameterDescriptor,
    FieldNamePatternParameterDescriptor,
    PatternTypeChoice,
    Locale,
    TranslationMapping
} from "@keyscore-manager-models";

@Injectable({providedIn: 'root'})
export class DeserializerService {
    constructor(private translateService: TranslateService) {
    }

    private selectLanguage(languages: string[]) {

        const lang = this.translateService.currentLang;

        return languages.includes(lang) ? lang :
            languages.includes('en') ? 'en' : languages[0];
    }

    deserializeDescriptor(descriptor: Descriptor): FilterDescriptor {
        const filterDescriptor: FilterDescriptorWithLocales = descriptor.describes;

        const currentLang = this.selectLanguage(descriptor.localization.locales.map(loc => loc.language));
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
            case ParameterDescriptorJsonClass.ExpressionParameterDescriptor: {
                const choices = parameterDescriptor.choices.map(choice => this.resolveChoice(settings, choice)).map(choice => new ExpressionParameterChoice(choice.name, choice.displayName, choice.description));
                return new ExpressionParameterDescriptor(base.ref, base.info.displayName, base.info.description, parameterDescriptor.defaultValue, parameterDescriptor.mandatory, choices);
            }
            case ParameterDescriptorJsonClass.NumberParameterDescriptor:
                return new NumberParameterDescriptor(base.ref, base.info.displayName, base.info.description,
                    parameterDescriptor.defaultValue, parameterDescriptor.range, parameterDescriptor.mandatory);
            case ParameterDescriptorJsonClass.DecimalParameterDescriptor:
                return new DecimalParameterDescriptor(base.ref, base.info.displayName, base.info.description,
                    parameterDescriptor.defaultValue, parameterDescriptor.range, parameterDescriptor.decimals,
                    parameterDescriptor.mandatory);
            case ParameterDescriptorJsonClass.FieldNameParameterDescriptor: {
                const validator = this.resolveValidator(settings, parameterDescriptor.validator);

                return new FieldNameParameterDescriptor(base.ref, base.info.displayName, base.info.description,
                    parameterDescriptor.defaultValue, parameterDescriptor.hint, validator, parameterDescriptor.mandatory);
            }
            case ParameterDescriptorJsonClass.FieldParameterDescriptor: {
                const validator = this.resolveValidator(settings, parameterDescriptor.validator);

                return new FieldParameterDescriptor(base.ref, base.info.displayName, base.info.description,
                    parameterDescriptor.defaultValue, parameterDescriptor.hint, validator,
                    parameterDescriptor.fieldValueType, parameterDescriptor.mandatory);
            }
            case ParameterDescriptorJsonClass.TextListParameterDescriptor: {
                const textDescriptor: TextParameterDescriptor =
                    this.resolveParameterDescriptor(settings, parameterDescriptor.descriptor) as TextParameterDescriptor;

                return new TextListParameterDescriptor(base.ref, base.info.displayName, base.info.description,
                    textDescriptor, parameterDescriptor.min, parameterDescriptor.max);
            }
            case ParameterDescriptorJsonClass.FieldNameListParameterDescriptor: {
                const fieldNameDescriptor =
                    this.resolveParameterDescriptor(settings, parameterDescriptor.descriptor) as FieldNameParameterDescriptor;

                return new FieldNameListParameterDescriptor(base.ref, base.info.displayName, base.info.description,
                    fieldNameDescriptor, parameterDescriptor.min, parameterDescriptor.max);
            }
            case ParameterDescriptorJsonClass.FieldListParameterDescriptor: {
                const fieldDescriptor = this.resolveParameterDescriptor(settings, parameterDescriptor.descriptor) as FieldParameterDescriptor;

                return new FieldListParameterDescriptor(base.ref, base.info.displayName, base.info.description,
                    fieldDescriptor, parameterDescriptor.min, parameterDescriptor.max);

            }
            case ParameterDescriptorJsonClass.ChoiceParameterDescriptor: {
                const choices: Choice[] = parameterDescriptor.choices.map(choice => this.resolveChoice(settings, choice));

                return new ChoiceParameterDescriptor(base.ref, base.info.displayName, base.info.description,
                    parameterDescriptor.min, parameterDescriptor.max, choices);
            }
            case ParameterDescriptorJsonClass.BooleanParameterDescriptor:
                return new BooleanParameterDescriptor(base.ref, base.info.displayName, base.info.description,
                    parameterDescriptor.defaultValue, parameterDescriptor.mandatory);
            case ParameterDescriptorJsonClass.FieldNamePatternParameterDescriptor:
                const supports = parameterDescriptor.supports.map(pattern => PatternTypeChoice.fromPatternType(pattern));
                return new FieldNamePatternParameterDescriptor(base.ref, base.info.displayName, base.info.description, parameterDescriptor.defaultValue, parameterDescriptor.hint, supports, parameterDescriptor.mandatory);
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
        let mapping: TranslationMapping = settings.descriptor.localization.mapping[key] ?
            settings.descriptor.localization.mapping[key] : undefined;

        if (mapping === undefined) return "";

        const possibleLanguages = Array.from(mapping.translations.keys());
        const language = possibleLanguages.includes(settings.language) ?
            settings.language : this.selectLanguage(possibleLanguages);


        return settings.descriptor.localization.mapping[key].translations[language];


    }
}