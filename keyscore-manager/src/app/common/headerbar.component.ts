import {Component, Input} from "@angular/core";

@Component({
    selector: "header-bar",
    template: `
        <div style="background-color: rgb(54, 88, 128); color: white; padding-left: -15px">
            <div class="p-2">
                <h5 class="font-weight-bold">{{title}}</h5>
            </div>
        </div>
    `
})
export class HeaderBarComponent {
    @Input() public title: string;
}
