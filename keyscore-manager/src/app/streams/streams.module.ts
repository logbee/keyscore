import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {StreamsComponent} from "./streams.component";

export const routes: Routes = [
    {
        path: 'stream',
        component: StreamsComponent,
    }
];

@NgModule({
    imports: [
        RouterModule.forChild(routes)
    ],
    declarations: [
        StreamsComponent
    ],
    providers: []
})
export class StreamsModule { }