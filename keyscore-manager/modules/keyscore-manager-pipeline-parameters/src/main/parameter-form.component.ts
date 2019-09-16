import {
    Component,
    EventEmitter,
    HostBinding,
    Input,
    OnDestroy,
    OnInit,
    Output,
    ViewChild,
    ViewContainerRef
} from "@angular/core";
import {ParameterComponentFactoryService} from "./service/parameter-component-factory.service";
import {Parameter, ParameterMap} from "@keyscore-manager-models/src/main/parameters/parameter.model";
import {of, Subject, Subscription} from "rxjs";
import {takeUntil} from "rxjs/operators";

@Component({
    selector: 'parameter-form',
    template: `
        <ng-template #formContainer></ng-template>
    `,
    styleUrls:['./parameter-form.component.scss']
})
export class ParameterFormComponent implements OnInit, OnDestroy {

    @Input() set parameters(val: ParameterMap) {
        this._parameters = val;
        this.createParameterComponents();
    };

    get parameters(): ParameterMap {
        return this._parameters;
    }

    private _parameters: ParameterMap;
    @Input() autoCompleteDataList: string[] = [];

    @Output() onValueChange: EventEmitter<Parameter> = new EventEmitter();

    @ViewChild("formContainer", {read: ViewContainerRef}) formContainer: ViewContainerRef;

    private unsubscribe$: Subject<void> = new Subject<void>();

    constructor(private parameterComponentFactory: ParameterComponentFactoryService) {
    }

    ngOnInit() {

    }

    createParameterComponents() {
        this.unsubscribe$.next();
        this.formContainer.clear();
        if(this.parameters && this.parameters.parameters) {
            Object.entries(this.parameters.parameters).forEach(([ref, [parameter, descriptor]]) => {
                const componentRef = this.parameterComponentFactory.createParameterComponent(descriptor.jsonClass, this.formContainer);
                componentRef.instance.parameter = parameter;
                componentRef.instance.descriptor = descriptor;
                componentRef.instance.autoCompleteDataList = this.autoCompleteDataList;

                componentRef.instance.emitter.pipe(takeUntil(this.unsubscribe$)).subscribe(parameter =>
                    this.onValueChange.emit(parameter));

            })
        }
    }

    ngOnDestroy() {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }


}