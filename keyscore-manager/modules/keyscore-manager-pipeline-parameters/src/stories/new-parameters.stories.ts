import {moduleMetadata, storiesOf} from "@storybook/angular";
import {ExpressionParameterComponent} from "../main/parameters/expression-parameter/expression-parameter.component";
import {MaterialModule} from "keyscore-manager-material";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {of} from "rxjs";
import {
    ExpressionParameterChoice,
    ExpressionParameter,
    ExpressionParameterDescriptor,
} from "../main/parameters/expression-parameter/expression-parameter.model";

storiesOf('Parameters/ExpressionParameter', module)
    .addDecorator(
        moduleMetadata({
            declarations: [],
            imports: [
                CommonModule,
                MaterialModule,
                BrowserAnimationsModule
                // ExpressionParameterModule
            ],
            providers: []
        }))
    .add("default", () => ({
        component: ExpressionParameterComponent,
        props: {
            descriptor$: of(new ExpressionParameterDescriptor("myexpression", "Field Pattern", "", "", [
                    new ExpressionParameterChoice("expression.regex", "RegEx", ""),
                    new ExpressionParameterChoice("expression.grok", "Grok", ""),
                    new ExpressionParameterChoice("expression.glob", "Glob", "")
                ]),
            ),
            parameter$: of(new ExpressionParameter("myexpression", "Hello World", "regex"))
        }
    }));
