import {Component, EventEmitter, Input, Output, ViewChild} from "@angular/core";
import {Range} from "keyscore-manager-models";
import {MatSlider} from "@angular/material";
import {FormControl} from "@angular/forms";

@Component({
    selector: `ks-slider`,
    template: `
        <div class="slider-label">
            {{label}}
        </div>
        <div class="ks-slider" fxLayout="row" fxLayoutGap="5">
            <span class="slider-label" fxFlexAlign="center">{{range.start}}</span>
            <mat-slider class="ks-slider" fxFlex #sliderInput [disabled]="disabled" [thumbLabel]="true"
                        [min]="range.start"
                        [max]="range.end" [step]="range.step" 
                        (change)="onChange(sliderInput.value)" [value]="value"></mat-slider>
            <span class="slider-label" fxFlexAlign="center">{{range.end}}</span>
        </div>
    `
})
export class SliderInputComponent {
    @Input() range: Range;
    @Input() label: string;
    @Input() disabled:boolean = false;
    @Input()
    get value():number{
        return this.slider.value;
    }
    set value(val:number){
        this.slider.value = val;
    }
    @Output()changed:EventEmitter<number> = new EventEmitter<number>();

    @ViewChild(MatSlider) slider:MatSlider;

    onChange(value:number){
        this.changed.emit(value);
    }
}