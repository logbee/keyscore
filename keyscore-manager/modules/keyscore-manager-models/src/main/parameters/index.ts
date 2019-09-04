/**
 * @module
 * @description
 * Starting point to import all Parameter related models
 */
import {JSONCLASS_BOOLEAN_DESCR} from "@keyscore-manager-models";

export * from './boolean-parameter.model'
export * from './number-parameter.model'
export * from './text-parameter.model'
export * from './parameter.model'
export * from './choice-parameter.model'
export * from './decimal-parameter.model'
export * from './expression-parameter.model'
export * from './field-name-parameter.model'
export * from './field-name-pattern-parameter.model'
export * from './field-parameter.model'
export * from './parameter-lists/field-list-parameter.model'
export * from './parameter-lists/text-list-parameter.model'
export * from './parameter-lists/field-name-list-parameter.model'
export * from './parameter-lists/list-parameter.model'
export * from './parameter-fields.model'

export enum ParameterDescriptorJsonClass{
    BooleanParameterDescriptor = "io.logbee.keyscore.model.descriptor.BooleanParameterDescriptor",
    NumberParameterDescriptor = "io.logbee.keyscore.model.descriptor.NumberParameterDescriptor",
    TextParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextParameterDescriptor",
    ChoiceParameterDescriptor = "io.logbee.keyscore.model.descriptor.ChoiceParameterDescriptor",
    DecimalParameterDescriptor = "io.logbee.keyscore.model.descriptor.DecimalParameterDescriptor",
    ExpressionParameterDescriptor = "io.logbee.keyscore.model.descriptor.ExpressionParameterDescriptor",
    FieldNameParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameParameterDescriptor",
    FieldNamePatternParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNamePatternParameterDescriptor",
    FieldParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldParameterDescriptor",
    FieldListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldListParameterDescriptor",
    FieldNameListParameterDescriptor = "io.logbee.keyscore.model.descriptor.FieldNameListParameterDescriptor",
    TextListParameterDescriptor = "io.logbee.keyscore.model.descriptor.TextListParameterDescriptor"


}