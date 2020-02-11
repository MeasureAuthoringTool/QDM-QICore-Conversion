package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.cql.CqlParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Library;

import java.util.Date;

@Slf4j
public class FhirLibraryTranslator extends LibraryTranslatorBase {
    private final CqlParser cqlParser;

    public FhirLibraryTranslator(byte[] cql, byte[] elm, String baseURL) {
        super(cql, elm, baseURL);
        cqlParser = new CqlParser(new String(cql));
    }


    @Override
    void translate(String version, Library fhirLibrary) {
        CqlParser.LibraryProperties libraryProperties = cqlParser.getLibrary();

        String uuid = createLibraryUuid(libraryProperties);
        log.info("uuid: {} from: {}", uuid, libraryProperties);

        fhirLibrary.setId(uuid);
        fhirLibrary.setApprovalDate(new Date());
        fhirLibrary.setName(libraryProperties.getName());
        fhirLibrary.setUrl(baseURL + "Library/" + uuid);

        if (StringUtils.isNotEmpty(version)) {
            fhirLibrary.setVersion(version);
        } else {
            fhirLibrary.setVersion(libraryProperties.getVersion());
        }
    }
}
