import {EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {Observable, Subscription} from "rxjs";

export abstract class ParameterComponent<D, P> implements OnInit, OnDestroy {

    @Input('descriptor')
    public descriptor: D;

    @Input('parameter')
    public parameter: P;

    @Input('autoCompleteDataList')
    public autoCompleteDataList: string[];

    @Output('parameter')
    public emitter = new EventEmitter<P>();

    get value(): P {
        return this.parameter;
    }

    ngOnInit(): void {
        this.onInit();
    }

    ngOnDestroy(): void {
        this.onDestroy();
    }

    protected emit(parameter: P): void {
        this.emitter.emit(parameter)
    }

    protected onInit(): void {
    }

    protected onDestroy(): void {
    }

}