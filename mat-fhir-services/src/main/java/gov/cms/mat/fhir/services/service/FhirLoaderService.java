package gov.cms.mat.fhir.services.service;


import gov.cms.mat.fhir.services.exceptions.HapiCreateException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class FhirLoaderService {
    private final HapiFhirServer hapiFhirServer;

    public FhirLoaderService(HapiFhirServer hapiFhirServer) {
        this.hapiFhirServer = hapiFhirServer;
    }

    public void load() {
        //loadDataElements();

        loadValueSets();

        loadV2Tables();

        loadV3CodeSsytems();

        // loadProfileResources();

        // loadConceptMaps();
    }


    private void loadV3CodeSsytems() {
        processFhirResourceBundleFile("v3-codesystems.json");
    }

    private void loadV2Tables() {
        processFhirResourceBundleFile("v2-tables.json");
    }

    private void loadValueSets() {
        processFhirResourceBundleFile("valuesets.json");
    }


    private void loadProfileResources() {
        processFhirResourceBundleFile("profiles-resources.json");
    }

    private void loadDataElements() {
        processFhirResourceBundleFile("dataelements.json");
    }

    private void loadConceptMaps() {
        processFhirResourceBundleFile("conceptmaps.json");
    }

    private void processFhirResourceBundleFile(String name) {
        String content = getData(name);
        log.info("{}  length: {}", name, String.format("%,d", content.length()));
        log.debug("{} contents: {}", name, content);

        processBundle(content);
    }

    private String getData(String name) {
        log.debug("Processing include file: {} ", name);

        try (InputStream i = getClass().getResourceAsStream("/fhir-load/" + name)) {
            return new String(i.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void processBundle(String content) {
        Bundle bundle = hapiFhirServer.parseResource(Bundle.class, content);

        if (bundle.hasEntry()) {
            log.info("Found in bundle {} {} objects", bundle.getEntry().size(), bundle.getEntry().get(0).getResource().getResourceType());
            bundle.getEntry().forEach(r -> createAndExecuteBundle(r.getResource()));
        } else {
            log.error("Bundle does not have any entries");
        }
    }

    private void createAndExecuteBundle(Resource resource) {

        try {

            Bundle bundleExecuted = hapiFhirServer.createAndExecuteBundle(resource);
            checkCreatedBundle(bundleExecuted);

        } catch (Exception e) {
            log.info("Cannot process resource : \n{}", hapiFhirServer.toJson(resource));
            log.error("Cannot save resource", e);
        }
    }

    private void checkCreatedBundle(Bundle bundleExecuted) {
        if (bundleExecuted.hasEntry() && bundleExecuted.getEntry().size() == 1) {

            String status = bundleExecuted.getEntry().get(0).getResponse().getStatus();

            if (status.equals("200 OK") || status.equals("201 Created")) {
                log.trace("Created Bundle is good");
            } else {
                throw new HapiCreateException("Create bundle has invalid status : " + status);
            }

        } else {
            throw new HapiCreateException("Create bundle created entries: " + bundleExecuted.getEntry().size());
        }
    }


}
