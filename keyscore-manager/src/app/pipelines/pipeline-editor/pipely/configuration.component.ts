import {Component, Input} from "@angular/core";

@Component({
    selector:"configurator",
    template:`
        <mat-sidenav #sidenav #right position="end" mode="over" [(opened)]="opened">
            testcontent
        </mat-sidenav>
    `
})

export class ConfigurationComponent{
    @Input() opened:boolean;

}