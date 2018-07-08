import {Component} from "@angular/core";
import {Store} from "@ngrx/store";
import {ModalService} from "../services/modal.service";
import {AppState} from "../app.component";
import {BOOLEAN_ITEM, getSettings, SettingsItem, SettingsModel, TEXT_CHOICE_ITEM} from "./settings.model";
import {Observable} from "rxjs/index";

@Component({
    selector: "keyscore-settings",
    providers: [ModalService],
    template: `
        <h4 class="">{{'SETTINGS.TITLE' | translate}}</h4>
        <div *ngFor="let group of (settings$ | async).groups" class="card mt-3">
            <div class="card-header">
                <span class="mb-0">{{group.title | translate}}</span>
            </div>
            <div class="card-body">
                <div class="container">
                    <div *ngFor="let item of group.items" class="row mb-2">
                        <div [ngSwitch]="item.type">
                            <div *ngSwitchCase="'[SettingsItem] Text'">
                                <span class="font-weight-bold">{{item.title | translate}}:&nbsp;</span>
                                <input class="form-control" [type]="'text'">
                            </div>
                            <div *ngSwitchCase="'[SettingsItem] TextChoice'">
                                <span class="font-weight-bold">{{item.title | translate}}:&nbsp;</span>
                            </div>
                            <div *ngSwitchCase="'[SettingsItem] Boolean'" class="toggleCheckbox">
                                <input type="checkbox" id="checkbox{{item.name}}" class="ios-toggle">
                                <label for="checkbox{{item.name}}" class="checkbox-label" data-off="">
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

    constructor(private store: Store<AppState>,
                private modalService: ModalService) {

        this.settings$ = store.select(getSettings);
    }

    protected close() {
        this.modalService.close();
    }

    protected isTextItem(item: SettingsItem) {
        return TEXT_CHOICE_ITEM === item.type;
    }

    protected isBooleanItem(item: SettingsItem) {
        return BOOLEAN_ITEM === item.type;
    }

    protected isTextChoiceItem(item: SettingsItem) {
        return TEXT_CHOICE_ITEM === item.type;
    }
}
