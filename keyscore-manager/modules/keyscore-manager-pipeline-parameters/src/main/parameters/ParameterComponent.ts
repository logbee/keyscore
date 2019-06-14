import {EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {Observable, Subscription} from "rxjs";

export abstract class ParameterComponent<D, P> implements OnInit, OnDestroy {

    @Input('descriptor')
    protected descriptor$: Observable<D>;

    @Input('parameter')
    protected parameter$: Observable<P>;

    @Output('parameter')
    protected emitter = new EventEmitter<P>();

    private descriptorSubscription: Subscription;
    private parameterSubscription: Subscription;

    ngOnInit(): void {
        this.descriptorSubscription = this.descriptor$.subscribe(descriptor => {
            this.onDescriptorChange(descriptor)
        });
        this.parameterSubscription = this.parameter$.subscribe(parameter => {
            this.onParameterChange(parameter)
        });
        this.onInit();
    }

    ngOnDestroy(): void {
        this.onDestroy();
        if (this.descriptorSubscription) this.descriptorSubscription.unsubscribe();
        if (this.parameterSubscription) this.parameterSubscription.unsubscribe();
    }

    protected emit(parameter: P): void {
        this.emitter.emit(parameter)
    }

    protected onInit(): void {}

    protected onDestroy(): void {}

    protected onDescriptorChange(descriptor: D): void {}

    protected onParameterChange(parameter: P): void {}
}