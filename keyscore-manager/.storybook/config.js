import {configure} from '@storybook/angular';
import "../src/styles/styles.scss";

// automatically import all files ending in *.stories.ts
const reqModules = require.context('../modules/', true, /.stories.ts$/);
const reqMain = require.context('../src/stories/', true, /.stories.ts$/);

function loadStories() {
  reqModules.keys().forEach(filename => reqModules(filename));
  reqMain.keys().forEach(filename => reqMain(filename));
}

configure(loadStories, module);
