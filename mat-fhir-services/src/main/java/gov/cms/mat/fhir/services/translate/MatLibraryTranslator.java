package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Library;
import org.springframework.lang.Nullable;

@Slf4j
public class MatLibraryTranslator extends LibraryTranslatorBase {

    private final CqlLibrary cqlLibrary;

    public MatLibraryTranslator(CqlLibrary cqlLibrary, byte[] cql, byte[] elm, String baseURL) {
        super(cql, elm, baseURL);

        this.cqlLibrary = cqlLibrary;
    }

    @Override
    public void translate(@Nullable String version, Library fhirLibrary) {
        fhirLibrary.setId(cqlLibrary.getId());
        fhirLibrary.setApprovalDate(cqlLibrary.getFinalizedDate());


        if (StringUtils.isNotEmpty(version)) {
            fhirLibrary.setVersion(version);
        } else if (cqlLibrary.getVersion() != null) {
            fhirLibrary.setVersion(cqlLibrary.getVersion().toString());
        }

        fhirLibrary.setName(cqlLibrary.getCqlName());
        fhirLibrary.setUrl(baseURL + "Library/" + cqlLibrary.getId());

        if (fhirLibrary.getContent().isEmpty()) {
            ConversionReporter.setLibraryFieldConversionResult("MAT.cql", "Library.content",
                    "No CQL or ELM to process", cqlLibrary.getId());
        }
    }
}
