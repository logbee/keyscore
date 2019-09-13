import {
    Component,
    ElementRef,
    EventEmitter,
    HostBinding,
    Input,
    OnDestroy,
    OnInit,
    Output,
    ViewChild
} from "@angular/core";
import {FormControl, NgControl} from "@angular/forms";
import {Observable, Subject} from "rxjs";
import {map, startWith} from "rxjs/operators";
import {MatAutocompleteTrigger, MatFormFieldControl} from "@angular/material";
import {FocusMonitor} from "@angular/cdk/a11y";
import {coerceBooleanProperty} from "@angular/cdk/coercion";

@Component({
    selector: 'ks-autocomplete-input',
    template: `
        <input #inputField #trigger="matAutocompleteTrigger" matInput type="text" [formControl]="inputControl"
               [matAutocomplete]="auto" (keyup.enter)="onEnter($event)">
        <mat-autocomplete #auto="matAutocomplete" (optionSelected)="onChange()">
            <mat-option *ngFor="let item of (filteredOptions | async)" [value]="item">{{item}}
            </mat-option>
        </mat-autocomplete>
    `,
    providers: [{provide: MatFormFieldControl, useExisting: AutocompleteFilterComponent}]
})
export class AutocompleteFilterComponent extends MatFormFieldControl<string> implements OnInit, OnDestroy {

    static nextId = 0;

    @HostBinding() id = `ks-autocomplete-input-${AutocompleteFilterComponent.nextId++}`;
    @HostBinding('attr.aria-describedby') describedBy = '';
    stateChanges: Subject<void> = new Subject<void>();
    inputControl = new FormControl();
    filteredOptions: Observable<string[]>;
    focused = false;
    ngControl: NgControl = null;
    errorState = false;

    @Input()
    get value(): string {
        return this.inputControl.value;
    }

    set value(val: string) {
        this.inputControl.setValue(val);
        this.stateChanges.next();
    }

    @Input()
    get placeholder() {
        return this._placeholder;
    }

    set placeholder(plh) {
        this._placeholder = plh;
        this.stateChanges.next();
    }

    private _placeholder: string;

    @Input()
    get disabled(): boolean {
        return this._disabled;
    }

    set disabled(value: boolean) {
        this._disabled = coerceBooleanProperty(value);
        this._disabled ? this.inputControl.disable() : this.inputControl.enable();
        this.stateChanges.next();
    }

    private _disabled = false;

    @Input() options: string[];

    get empty() {
        return !this.inputControl.value;
    }

    @HostBinding('class.floating')
    get shouldLabelFloat() {
        return this.focused || !this.empty;
    }

    @Output() change: EventEmitter<void> = new EventEmitter<void>();
    @Output() keyUpEnterEvent: EventEmitter<Event> = new EventEmitter();

    @ViewChild('inputField') inputElemRef: ElementRef;
    @ViewChild(MatAutocompleteTrigger) autocompleteTrigger: MatAutocompleteTrigger;

    constructor(private fm: FocusMonitor, private elRef: ElementRef<HTMLElement>) {
        super();
        fm.monitor(elRef.nativeElement, true).subscribe(origin => {
            this.focused = !!origin;
            this.stateChanges.next();
        });
    }

    ngOnInit() {
        this.filteredOptions = this.inputControl.valueChanges
            .pipe(
                startWith(''),
                map(value => this.filter(value))
            );
    }

    focus(event: Event) {
        if (event) {
            event.stopPropagation();
        }
        this.inputElemRef.nativeElement.focus();
        this.autocompleteTrigger.openPanel();
    }

    public clear() {
        this.value = '';
        this.onChange();
        this.focus(null);
    }

    private onEnter(event: Event) {
        this.keyUpEnterEvent.emit(event);
    }

    private filter(value: string): string[] {
        if (!this.options) return [];

        const filterValue = value.toLowerCase();

        return this.options.filter(option => option.toLowerCase().includes(filterValue));
    }

    private onChange() {
        this.stateChanges.next();
        this.change.emit();
    }

    onContainerClick(event: MouseEvent) {

    }

    setDescribedByIds(ids: string[]): void {
        this.describedBy = ids.join(' ');
    }

    ngOnDestroy() {
        this.stateChanges.complete();
        this.fm.stopMonitoring(this.elRef);
    }

}