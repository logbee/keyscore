import {
    AfterViewInit,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnDestroy,
    Output,
    QueryList,
    ViewChild,
    ViewChildren
} from "@angular/core";

import {MatExpansionPanel} from "@angular/material/expansion";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {Subject} from "rxjs";
import {takeUntil} from "rxjs/operators";
import {Icon, IconEncoding, IconFormat} from '@keyscore-manager-models/src/main/descriptors/Icon';
import {RippleAnimationConfig} from "@angular/material/core";

export interface MenuItem {
    id: string,
    displayName: string,
    description?: string,
    icon?: Icon
}

@Component({
    selector: 'ks-button-add-directive',
    template: `

        <mat-expansion-panel hideToggle class="add-directive-wrapper" [disabled]="hasNoMenuItems()">

            <mat-expansion-panel-header *ngIf="!hasNoMenuItems();else withOutItems"
                                        [@openClose]="_panelExpanded ? 'open' : 'closed'"
                                        class="add-directive-header">
                <mat-panel-title class="add-button">
                    <mat-icon color="accent">add_circle_outline</mat-icon>
                </mat-panel-title>
            </mat-expansion-panel-header>

            <ng-template #withOutItems>
                <mat-expansion-panel-header
                        class="add-directive-header"
                        matRipple [matRippleColor]="rippleColor" (click)="add(null)">
                    <mat-panel-title class="add-button">
                        <mat-icon color="accent">add_circle_outline</mat-icon>
                    </mat-panel-title>
                </mat-expansion-panel-header>

            </ng-template>

            <div class="add-menu">
                <div *ngFor="let item of itemsToAdd"
                     class="add-menu-item" fxLayout="row"
                     fxLayoutGap="8px"
                     fxLayoutAlign="start center"
                     [matTooltip]="item.description"
                     matRipple
                     [matRippleAnimation]="_rippleMenuItemConfig"
                     (click)=add(item)>
                    <div class="icon-wrapper" #iconContainer></div>
                    <span>{{item.displayName}}</span>
                </div>
            </div>

        </mat-expansion-panel>


    `,
    styleUrls: ['./add-directive.component.scss'],
    animations: [
        trigger('openClose', [
            state('open', style({
                transform: 'rotateX(90deg)',
                height: '0px',
                opacity: 1
            })),
            state('closed', style({
                transform: 'rotateX(0deg)',
                opacity: 1,
                height: '48px'
            })),
            transition('open => closed', [
                animate('0.3s ease-out')
            ]),
            transition('closed => open', [
                animate('0.3s ease-in')
            ])
        ])
    ]
})
export class AddDirectiveComponent implements AfterViewInit, OnDestroy {

    @Input() itemsToAdd: MenuItem[];
    @Input() rippleColor: string = "rgba(236,64,122,0.15)";
    @Input() autoClosePanelOnAdd: boolean = true;

    @Output() onAdd: EventEmitter<MenuItem> = new EventEmitter();

    @ViewChild(MatExpansionPanel) panel: MatExpansionPanel;
    @ViewChildren('iconContainer') iconContainers: QueryList<ElementRef>;

    private _panelExpanded: boolean;
    private _unsubscribe$: Subject<void> = new Subject();

    private _rippleMenuItemConfig: RippleAnimationConfig = {
        enterDuration: 0.2,
        exitDuration: 0.2
    };

    constructor() {
    }

    ngAfterViewInit(): void {
        this.insertDirectiveIcons();
        this.iconContainers.changes.pipe(takeUntil(this._unsubscribe$)).subscribe(() => this.insertDirectiveIcons());
        this.panel.expandedChange.pipe(takeUntil(this._unsubscribe$)).subscribe((val) => this._panelExpanded = val);
    }

    private insertDirectiveIcons() {
        this.iconContainers.forEach((container, index) => {
            const item = this.itemsToAdd[index];
            if (item.icon && item.icon.encoding === IconEncoding.RAW && item.icon.format === IconFormat.SVG) {
                container.nativeElement.innerHTML = this.itemsToAdd[index].icon.data;
            }
        })
    }

    public closePanel() {
        this.panel.close();
    }

    private add(item: MenuItem) {
        if (this.autoClosePanelOnAdd) {
            this.closePanel();
        }
        this.onAdd.emit(item);
    }

    private hasNoMenuItems() {
        return !(this.itemsToAdd && this.itemsToAdd.length > 0);
    }

    private onClick() {
        if (this.hasNoMenuItems()) {
            this.onAdd.emit(null);
        }
    }

    ngOnDestroy(): void {
        this._unsubscribe$.next();
        this._unsubscribe$.complete();
    }
}
