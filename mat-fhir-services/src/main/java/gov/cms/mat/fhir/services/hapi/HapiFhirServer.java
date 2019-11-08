package gov.cms.mat.fhir.services.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.SearchTotalModeEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static gov.cms.mat.fhir.services.translate.creators.FhirValueSetCreator.SYSTEM_IDENTIFIER;

@Component
@Slf4j
public class HapiFhirServer {
    // Performance: This class is expensive to instantiate, why in component which will create once
    @Getter
    FhirContext ctx;

    @Getter
    IGenericClient hapiClient;

    @Value("${fhir.r4.baseurl}")
    private String baseURL;

    @PostConstruct
    public void setUp() {
        ctx = FhirContext.forR4();

        hapiClient = ctx.newRestfulGenericClient(baseURL);
        hapiClient.registerInterceptor(createLoggingInterceptor());

        log.info("Created hapi client for server: {} ", baseURL);
    }

    public Bundle createBundle(Resource resource) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        bundle.addEntry().setResource(resource)
                .getRequest()
                .setUrl(baseURL + resource.getResourceType().name() + "/" + resource.getId())
                .setMethod(Bundle.HTTPVerb.PUT);

        return getHapiClient().transaction().withBundle(bundle).execute();
    }

    private LoggingInterceptor createLoggingInterceptor() {
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();

        // Optionally you may configure the interceptor (by default only
        // summary info is logged)
        loggingInterceptor.setLogRequestSummary(true);
        loggingInterceptor.setLogRequestBody(true);
        return loggingInterceptor;
    }

    public IBaseOperationOutcome delete(IBaseResource resource) {
        return hapiClient.delete()
                .resource(resource)
                .prettyPrint()
                .encodedJson()
                .execute();
    }

    public Bundle isValueSetInHapi(String oid) {
        return hapiClient.search()
                .forResource(ValueSet.class)
                .where(ValueSet.IDENTIFIER.exactly().systemAndCode(SYSTEM_IDENTIFIER, oid))
                .returnBundle(Bundle.class)
                .execute();
    }

    public int count(Class<? extends IBaseResource> resourceClass) {
        return hapiClient.search()
                .forResource(resourceClass)
                .totalMode(SearchTotalModeEnum.ACCURATE)
                .returnBundle(Bundle.class)
                .execute()
                .getTotal();
    }

    public Bundle getAll(Class<? extends IBaseResource> resourceClass) {
        return hapiClient.search()
                .forResource(resourceClass)
                .returnBundle(Bundle.class)
                .execute();
    }

    public Bundle getNextPage(Bundle bundle) {
        return hapiClient.loadPage()
                .next(bundle)
                .execute();
    }
}
