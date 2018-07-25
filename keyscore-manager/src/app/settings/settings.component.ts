import {Component} from "@angular/core";
import {Store} from "@ngrx/store";
import {ModalService} from "../services/modal.service";
import {AppState} from "../app.component";
import {
    BooleanItem,
    getModifiedSettings,
    getSettingsState,
    SettingsModel,
    TextChoiceItem,
    TextItem
} from "./settings.model";
import {Observable} from "rxjs/index";
import {TranslateService} from "@ngx-translate/core";
import {ApplySettingsAction, UpdateSettingsItemAction} from "./settings.actions";

@Component({
    selector: "keyscore-settings",
    providers: [ModalService],
    template: `
        <header-bar title="{{'SETTINGS.TITLE' | translate}}"></header-bar>
        <div *ngIf="isModified" class="btn-success" (click)="applySettings()">Apply</div>
        <div *ngFor="let group of (settings$ | async).groups" class="card m-3">
            <div class="card-header">
                <span class="mb-0">{{group.title | translate}}</span>
            </div>
            <div class="card-body">
                <div class="container">
                    <div *ngFor="let item of group.items" class="row mb-3">
                        <div [ngSwitch]="item.type" class="col">
                            <div *ngSwitchCase="'[SettingsItem] Text'">
                                <label for="input.{{item.name}}" class="font-weight-bold">
                                    {{item.title | translate}}:
                                </label>
                                <input id="input.{{item.name}}" class="form-control" [type]="'text'">
                            </div>
                            <div *ngSwitchCase="'[SettingsItem] TextChoice'">
                                <label for="dropdown.{{item.name}}" class="font-weight-bold mr-2">
                                    {{item.title | translate}}:
                                </label>
                                <span id="dropdown.{{item.name}}" class="dropdown">
                                    <button class="btn btn-light dropdown-toggle" type="button" data-toggle="dropdown">
                                        {{item.value.title | translate}}
                                    </button>
                                    <div class="dropdown-menu">
                                        <div *ngFor="let choice of item.choices" class="dropdown-item btn"
                                             (click)="updateTextChoiceItem(item, choice)">
                                            {{choice.title | translate}}
                                        </div>
                                    </div>
                                </span>
                            </div>
                            <div *ngSwitchCase="'[SettingsItem] Boolean'">
                                <input type="checkbox" id="checkbox.{{item.name}}" class="ios-toggle" [checked]="item.value" (change)="updateBooleanItem(item)">
                                <label for="checkbox.{{item.name}}" class="checkbox-label" data-off="">
                                    {{item.title | translate}}
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})

export class SettingsComponent {

    private settings$: Observable<SettingsModel>;
    public isModified: boolean;
    constructor(private store: Store<AppState>, private translate: TranslateService) {
        this.settings$ = store.select(getModifiedSettings);
        store.select(getSettingsState).subscribe( state => {
            this.isModified = state.isModified;
        });
    }

    private applySettings() {
        this.store.dispatch(new ApplySettingsAction());
    }

    private updateTextItem(item: TextItem) {
        console.log(item.value)
        this.store.dispatch(new UpdateSettingsItemAction(new TextItem(
            item.name, item.title, item.description, item.value
        )))
    }

    private updateBooleanItem(item: BooleanItem) {
        this.store.dispatch(new UpdateSettingsItemAction(new BooleanItem(
            item.name, item.title, item.description, !item.value
        )));
    }

    private updateTextChoiceItem(item: TextChoiceItem, choice: TextItem) {
        this.store.dispatch(new UpdateSettingsItemAction(new TextChoiceItem(
            item.name, item.title, item.description, choice, item.choices
        )));
    }
}
