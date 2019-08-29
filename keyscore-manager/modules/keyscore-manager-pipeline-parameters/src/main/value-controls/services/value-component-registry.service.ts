import {Injectable, Type} from "@angular/core";
import {FieldValueType} from "@keyscore-manager-models";
import {BooleanValueComponent} from "../boolean-value.component";
import {TextValueComponent} from "../text-value.component";
import {TimestampValueComponent} from "../timestamp-value.component";
import {DurationValueComponent} from "../duration-value.component";
import {NumberValueComponent} from "../number-value.component";
import {DecimalValueComponent} from "../decimal-value.component";

@Injectable({
    providedIn: 'root'
})
export class ValueComponentRegistryService {
    private registry: Map<FieldValueType, Type<any>> = new Map();


    public getValueComponent(valueType: FieldValueType): Type<any> {
        const component = this.registry.get(valueType);

        if (component) return component;

        throw Error(`No component found for ${valueType}. 
        Maybe you forgot to register the component at the ValueComponentRegistryService`);

    }

    constructor() {
        this.initRegistry();
    }

    private initRegistry() {
        this.registry.set(FieldValueType.Unknown, TextValueComponent);
        this.registry.set(FieldValueType.Boolean, BooleanValueComponent);
        this.registry.set(FieldValueType.Text, TextValueComponent);
        this.registry.set(FieldValueType.Timestamp, TimestampValueComponent);
        this.registry.set(FieldValueType.Duration, DurationValueComponent);
        this.registry.set(FieldValueType.Number, NumberValueComponent);
        this.registry.set(FieldValueType.Decimal, DecimalValueComponent);

    }


}