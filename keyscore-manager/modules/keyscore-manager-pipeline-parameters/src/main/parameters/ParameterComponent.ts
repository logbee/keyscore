import {EventEmitter, HostBinding, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {Observable, Subscription} from "rxjs";

export abstract class ParameterComponent<D, P> implements OnInit, OnDestroy {

    @Input() public descriptor: D;

    @Input() public parameter: P;

    @Input() public autoCompleteDataList: string[];

    @Input() public label:string;

    @Input() public showLabel:boolean = true;

    @Output('parameter') public emitter = new EventEmitter<P>();

    @Output() public keyUpEnterEvent = new EventEmitter<Event>();

    @HostBinding('style.width') width = '100%';

    get value(): P {
        return this.parameter;
    }

    ngOnInit(): void {
        this.onInit();
    }

    ngOnDestroy(): void {
        this.onDestroy();
    }

    public clear(): void{
    }

    protected emit(parameter: P): void {
        this.emitter.emit(parameter)
    }

    protected onInit(): void {
    }

    protected onDestroy(): void {
    }

}