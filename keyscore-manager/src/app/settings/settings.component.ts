import {Component} from "@angular/core";
import {Store} from "@ngrx/store";
import {ModalService} from "../services/modal.service";
import {AppState} from "../app.component";
import {getSettings, SettingsModel, SettingsState} from "./settings.model";
import {Observable} from "rxjs/index";

@Component({
    selector: "keyscore-settings",
    providers: [ModalService],
    template: `
        <div class="modal fade" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">{{'SETTINGS.DIALOG_TITLE' | translate}}</h5>
                        <button type="button" class="close" aria-label="Close" (click)="close()">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <div id="accordion">
                            <div *ngFor="let group of (settings$ | async).groups" class="card">
                                <div [attr.id]="'heading-' + group.name"
                                     class="card-header btn text-left" data-toggle="collapse"
                                     [attr.data-target]="'#collapse-' + group.name">
                                    <span class="mb-0">{{group.title | translate}}</span>
                                </div>
                                <div [attr.id]="'collapse-' + group.name" class="collapse" data-parent="#accordion">
                                    <div class="card-body">
                                        Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry
                                        richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard
                                        dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf
                                        moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla
                                        assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore
                                        wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher
                                        ice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic
                                        synth nesciunt you probably haven't heard of them accusamus
                                        labore sustainable VHS.
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary"
                                (click)="close()">{{'GENERAL.CLOSE' | translate}}
                        </button>
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
}
