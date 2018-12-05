import {Component} from "@angular/core";
import {Store} from "@ngrx/store";
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
    providers: [],
    template: `
        
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
