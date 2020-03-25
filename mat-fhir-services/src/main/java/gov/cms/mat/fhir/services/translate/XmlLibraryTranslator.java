package gov.cms.mat.fhir.services.translate;

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Library;

import java.util.Date;

@Slf4j
public class XmlLibraryTranslator extends LibraryTranslatorBase {

    private final String uuid;
    private final String name;

    public XmlLibraryTranslator(String name, String cql, String elm, String baseURL, String uuid) {
        super(cql.getBytes(), elm.getBytes(), baseURL);
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public void translate(String version, Library fhirLibrary) {
        fhirLibrary.setId(uuid);
        fhirLibrary.setApprovalDate(new Date()); // TODO -- is this okay can leave null or MT, cannot find in XML
        fhirLibrary.setVersion(version);
        fhirLibrary.setName(name);
        fhirLibrary.setUrl(baseURL + "Library/" + uuid);
    }
}
