package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.rest.dto.cql.CqlPayload;
import gov.cms.mat.fhir.rest.dto.cql.CqlPayloadType;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.exceptions.ExternalHapiLibraryNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.CqlVersionConverter;
import gov.cms.mat.fhir.services.summary.CqlLibraryFindData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static gov.cms.mat.fhir.services.translate.LibraryTranslatorBase.CQL_CONTENT_TYPE;

@Service
@Slf4j
public class LibraryFinderService implements CqlVersionConverter {
    private final CqlLibraryDataService cqlLibraryDataService;
    private final HapiFhirServer hapiFhirServer;

    public LibraryFinderService(CqlLibraryDataService cqlLibraryDataService, HapiFhirServer hapiFhirServer) {
        this.cqlLibraryDataService = cqlLibraryDataService;
        this.hapiFhirServer = hapiFhirServer;
    }

    public CqlPayload findLibrary(CqlLibraryFindData data) {
        CqlLibrary cqlLibrary = cqlLibraryDataService.findCqlLibrary(data);

        if (cqlLibrary.getDraft() == null || !cqlLibrary.getDraft()) {
            return buildCqlPayload(data.getName(), data.getVersion());
        } else {
            return buildXmlPayload(cqlLibrary);
        }
    }

    public CqlPayload buildCqlPayload(String name, String version) {
        String data = getCqlFromFire(name, version);
        return CqlPayload.builder()
                .data(data)
                .type(CqlPayloadType.CQL)
                .build();
    }

    public CqlPayload buildXmlPayload(CqlLibrary cqlLibrary) {
        String data = getXmlFromMat(cqlLibrary);

        return CqlPayload.builder()
                .data(data)
                .type(CqlPayloadType.XML)
                .build();
    }

    public String getXmlFromMat(CqlLibrary cqlLibrary) {
        if (StringUtils.isEmpty(cqlLibrary.getCqlXml())) {
            throw new CqlLibraryNotFoundException("Xml is missing for library id: ", cqlLibrary.getId());
        } else {
            return cqlLibrary.getCqlXml();
        }
    }

    public String getCqlFromFire(String name, String version) {
        Bundle bundle = hapiFhirServer.fetchLibraryBundleByVersionAndName(version, name);

        if (CollectionUtils.isEmpty(bundle.getEntry())) {
            throw new ExternalHapiLibraryNotFoundException(name, version);
        } else {
            return processBundle(name, version, bundle);
        }
    }

    public String processBundle(String name, String version, Bundle bundle) {
        Optional<Library> optional = hapiFhirServer.findResourceInBundle(bundle, Library.class);

        if (optional.isPresent()) {
            String encoded = getCqlFromHapiLibrary(name, version, optional.get());
            byte[] cqlBytes = Base64.getDecoder().decode(encoded);
            return new String(cqlBytes);
        } else {
            throw new CqlLibraryNotFoundException("Cannot find library in bundle for name: " + name + ", version: " + version);
        }
    }

    public String getCqlFromHapiLibrary(String name, String version, Library library) {
        List<Attachment> attachments = library.getContent();

        if (CollectionUtils.isEmpty(attachments)) {
            throw new CqlLibraryNotFoundException("Cannot find attachments for library name: " + name + ", version: " + version);
        }

        Attachment cql = attachments.stream()
                .filter(a -> a.getContentType().equals(CQL_CONTENT_TYPE))
                .findFirst()
                .orElseThrow(() -> new CqlLibraryNotFoundException("Cannot find attachment type " + CQL_CONTENT_TYPE +
                        " for library  name: " + name + ", version: " + version));

        return new String(cql.getData());
    }

}


