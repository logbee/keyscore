import {Component} from '@angular/core'

@Component({
    selector: 'add-filter-dialog',
    template: `
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Modal title</h5>
                <button type="button" class="close" aria-label="Close" (click)="close()">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                ...
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" (click)="close()">Close</button>
                <button type="button" class="btn btn-primary" (click)="close()">Save changes</button>
            </div>
        </div>
    `
})

export class AddFilterDialog {

    // constructor(private modalService: ModalService) {
    // }
    //
    // close() {
    //     this.modalService.close()
    // }
}