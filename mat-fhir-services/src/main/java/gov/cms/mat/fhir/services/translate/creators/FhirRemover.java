package gov.cms.mat.fhir.services.translate.creators;

import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hl7.fhir.instance.model.api.IBaseBundle.LINK_NEXT;

public interface FhirRemover {

    default int deleteAllResource(HapiFhirServer hapiFhirServer, Class<? extends IBaseResource> resourceClass) {
        Bundle bundle = hapiFhirServer.getAll(resourceClass);

        AtomicInteger count = new AtomicInteger();

        while (bundle.hasEntry()) {
            bundle.getEntry().forEach(f -> {
                count.getAndIncrement();
                hapiFhirServer.delete(f.getResource());
            });

            if (bundle.getLink(LINK_NEXT) != null) {
                // load next page
                bundle = hapiFhirServer.getNextPage(bundle);
            } else {
                break;
            }
        }

        return count.get();
    }
}
