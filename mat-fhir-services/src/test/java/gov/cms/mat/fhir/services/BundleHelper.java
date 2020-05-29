package gov.cms.mat.fhir.services;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface BundleHelper {
    default Bundle createBundle(String url, Resource... resource) {
        Bundle bundle = new Bundle();

        AtomicInteger idCounter = new AtomicInteger(1);

        List<Bundle.BundleEntryComponent> bundleEntryComponents =
                Arrays.stream(resource)
                        .map(r -> buildBundleEntryComponent(url, r, idCounter.getAndIncrement()))
                        .collect(Collectors.toList());

        bundle.setEntry(bundleEntryComponents);

        return bundle;
    }

    private Bundle.BundleEntryComponent buildBundleEntryComponent(String url, Resource resource, int id) {
        resource.setId(String.format("%d", id));

        return new Bundle.BundleEntryComponent()
                .setResource(resource)
                .setFullUrl(url + "/" + resource.fhirType() + "/" + id);
    }
}
