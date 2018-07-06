import "bootstrap";
import "bootstrap/dist/css/bootstrap.css";
import * as $ from "jquery";
import "popper.js";
import "reflect-metadata";
import "zone.js";

import {platformBrowserDynamic} from "@angular/platform-browser-dynamic";

import {AppModule} from "./app/app.module";

platformBrowserDynamic().bootstrapModule(AppModule);

$(document).ready(() => {
    console.log("[main.ts] jquery ready.");
});
