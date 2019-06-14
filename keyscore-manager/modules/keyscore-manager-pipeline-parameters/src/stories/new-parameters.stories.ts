import {moduleMetadata, storiesOf} from "@storybook/angular";
import {ExpressionParameterComponent} from "../main/parameters/expression-parameter/expression-parameter.component";
import {ExpressionParameterModule} from "../main/parameters/expression-parameter/expression-parameter.module";

storiesOf('Parameters/ExpressionParameter', module).add("example", () => ({
    component: ExpressionParameterComponent,
    props: {

    }
}));
