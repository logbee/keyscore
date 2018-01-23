import 'zone.js';
import 'reflect-metadata';
import * as $ from 'jquery'
import 'popper.js'
import 'bootstrap'
import 'bootstrap/dist/css/bootstrap.css';

import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {AppModule} from './app/app.module';

platformBrowserDynamic().bootstrapModule(AppModule);

$(document).ready(function () {
    console.log("[main.ts] jquery ready.");
});