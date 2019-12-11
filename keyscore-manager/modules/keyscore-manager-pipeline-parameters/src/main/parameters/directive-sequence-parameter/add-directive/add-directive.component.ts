import {Component, EventEmitter, Input, Output} from "@angular/core";

export interface MenuItem {
    id: string,
    name: string,
    description?: string
}

@Component({
    selector: 'ks-button-add-directive',
    template: `
        <div *ngIf="itemsToAdd;else noMenu" class="add-directive-wrapper" fxFlex fxLayout="row"
             fxLayoutAlign="center center" matRipple
             [matRippleColor]="_rippleColor" [matMenuTriggerFor]="menu ">
            <mat-icon color="accent">add_circle_outline</mat-icon>
        </div>

        <ng-template #noMenu>
            <div class="add-directive-wrapper" fxFlex fxLayout="row" fxLayoutAlign="center center" matRipple
                 [matRippleColor]="_rippleColor" (click)="add(null)">
                <mat-icon color="accent">add_circle_outline</mat-icon>
            </div>
        </ng-template>

        <mat-menu #menu="matMenu" yPosition="below" xPosition="before">
            <button mat-menu-item *ngFor="let item of itemsToAdd" (click)="add(item)" [matTooltip]="item.description" matTooltipPosition="left">{{item.name}}</button>
        </mat-menu>
    `,
    styleUrls: ['./add-directive.component.scss']
})
export class AddDirectiveComponent {

    @Input() itemsToAdd: MenuItem[];
    @Output() onAdd: EventEmitter<MenuItem> = new EventEmitter();

    private _rippleColor: string = "rgba(236,64,122,0.15)";

    private add(item: MenuItem) {
        this.onAdd.emit(item);
    }
}
