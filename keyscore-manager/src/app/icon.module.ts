import {NgModule} from "@angular/core";
import {MatIconRegistry} from "@angular/material/icon";
import {DomSanitizer} from "@angular/platform-browser";

@NgModule()
export class IconModule {

    constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer) {

        this.matIconRegistry.addSvgIcon('source-stage', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/stages/source-stage.svg"));
        this.matIconRegistry.addSvgIcon('sink-stage', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/stages/sink-stage.svg"));
        this.matIconRegistry.addSvgIcon('filter-stage', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/stages/filter-stage.svg"));
        this.matIconRegistry.addSvgIcon('merge-stage', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/stages/merge-block.svg"));
        this.matIconRegistry.addSvgIcon('branch-stage', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/stages/branch-block.svg"));

        // custom data icons
        this.matIconRegistry.addSvgIcon('text-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/text-value.svg"));
        this.matIconRegistry.addSvgIcon('boolean-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/boolean-value.svg"));
        this.matIconRegistry.addSvgIcon('decimal-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/decimal-value.svg"));
        this.matIconRegistry.addSvgIcon('duration-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/duration-value.svg"));
        this.matIconRegistry.addSvgIcon('number-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/number-value.svg"));
        this.matIconRegistry.addSvgIcon('timestamp-icon', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/data/timestamp-value.svg"));

        // Custom Naviagation Icons
        this.matIconRegistry.addSvgIcon('navigate-to-pipely', this.domSanitizer.bypassSecurityTrustResourceUrl("/assets/images/pipeline/navigation/pipely-navigation.svg"));

        this.matIconRegistry.addSvgIcon('pipelines-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/pipeline-nav.svg'));
        this.matIconRegistry.addSvgIcon('agents-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/menu/agents.svg'));
        this.matIconRegistry.addSvgIcon('dashboard-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/speedometer.svg'));
        this.matIconRegistry.addSvgIcon('resources-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/menu/resources.svg'));
        this.matIconRegistry.addSvgIcon('documentation-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/menu/documentation.svg'));
        this.matIconRegistry.addSvgIcon('expand-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/chevron-right-round.svg'));
        this.matIconRegistry.addSvgIcon('collapse-nav', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/chevron-left-round.svg'));

        this.matIconRegistry.addSvgIcon('maturity-official', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/maturity-official.svg'));
        this.matIconRegistry.addSvgIcon('maturity-stable', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/maturity-stable.svg'));
        this.matIconRegistry.addSvgIcon('maturity-development', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/maturity-development.svg'));
        this.matIconRegistry.addSvgIcon('maturity-experimental', this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/misc/maturity-experimental.svg'));

        //Language Flags
        this.matIconRegistry.addSvgIcon('lang-de',this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/flags/de.svg'));
        this.matIconRegistry.addSvgIcon('lang-en',this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/flags/en.svg'));
    }
}
