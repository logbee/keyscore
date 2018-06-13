import {Component, Input} from "@angular/core";
import {FilterDescriptor} from "../pipelines.model";

@Component({
    selector:'filter-information',
    template:`
        <div class="card">
            <div class="card-header" style="font-size: 22px">{{selectedFilter.displayName}}</div>
            <div class="card-body">{{selectedFilter.description}}</div>
            <div class="card-footer"></div>
        </div>
    `
})

export class FilterInformationComponent {
    @Input() selectedFilter:FilterDescriptor;
}