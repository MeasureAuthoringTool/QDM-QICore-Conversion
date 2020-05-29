package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;

import java.util.Date;

import static gov.cms.mat.fhir.services.components.mongo.HapiResourcePersistedState.CREATED;

@Slf4j
public class XmlLibraryTranslator extends LibraryTranslatorBase {

    private final String uuid;
    private final String name;

    public XmlLibraryTranslator(String name, String cql, String elmJson,  String elmXml, String baseURL, String uuid) {
        super(cql.getBytes(), elmJson.getBytes(), elmXml.getBytes(), baseURL);
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public void translate(String version, Library fhirLibrary) {
        fhirLibrary.setId(uuid);
        fhirLibrary.setApprovalDate(new Date());
        fhirLibrary.setVersion(version);
        fhirLibrary.setName(name);
        fhirLibrary.setUrl(baseURL + "Library/" + uuid);

        ConversionReporter.setLibraryValidationLink(fhirLibrary.getUrl(), CREATED, uuid);
    }
}
