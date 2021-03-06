import {Descriptor} from "../../app/models/descriptors/Descriptor";
import {resolvedRemoveFieldsFilterDE} from "../fake-data/resolved-remove-fields-filter-descriptor";
import {ResolvedFilterDescriptor} from "../../app/models/descriptors/FilterDescriptor";
import {DescriptorResolverService} from "../../app/services/descriptor-resolver.service";
import {TranslateService} from "@ngx-translate/core";
import {TestBed} from "@angular/core/testing";

class MockTranslateService extends TranslateService {

}

describe('Service: DescriptorResolverService', () => {
    const testDescriptor: Descriptor = require('../fake-data/remove-fields-filter-descriptor.json');
    const expected: ResolvedFilterDescriptor = resolvedRemoveFieldsFilterDE;

    let translateService: TranslateService;
    let service: DescriptorResolverService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                DescriptorResolverService,
                {
                    provide: TranslateService,
                    useValue: {
                        currentLang: 'de'
                    }
                }
            ]
        });
        service = TestBed.get(DescriptorResolverService);
        translateService = TestBed.get(TranslateService);
    });


    it('should create', () => {
        expect(service).toBeTruthy();
    });

    describe('resolveDescriptor',() => {
        it('should resolve the given descriptor',() => {
            let result = service.resolveDescriptor(testDescriptor);
            expect(result).toEqual(expected);
        });


    })
});