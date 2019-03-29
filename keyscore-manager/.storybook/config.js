import { configure } from '@storybook/angular';
import "../src/app/style/style.scss"

// automatically import all files ending in *.stories.ts
const req = require.context('../', true, /.stories.ts$/);
function loadStories() {
  req.keys().forEach(filename => req(filename));
}

configure(loadStories, module);
