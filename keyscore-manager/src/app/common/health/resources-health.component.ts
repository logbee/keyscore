import {AfterViewInit, Component, Input, OnInit} from "@angular/core";
import {Store} from "@ngrx/store";
import {StateObject} from "../../models/common/StateObject";
import {selectStateObjects} from "../../resources/resources.reducer";
import {Observable} from "rxjs/index";
import {Health} from "../../models/common/Health";

@Component({
    selector: "resource-health",
    template: `
        <div matTooltipPosition="above" matTooltip="Health">
            <div class="health-light {{health}}"></div>
        </div>
    `,

})

export class ResourcesHealthComponent implements  OnInit {
    @Input() public uuid: string;
    @Input() public stateObjects$: Observable<StateObject[]>;

    private health: Health;
    constructor(private store: Store<any>) {
    }

    ngOnInit(): void {
        this.stateObjects$.subscribe(list => console.log(list.length));
    }
}