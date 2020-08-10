package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.cql.CQLAntlrUtils;
import gov.cms.mat.fhir.services.cql.LibraryCqlVisitorFactory;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.DataRequirement;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
@Setter
@Service
public class LibraryTranslator extends TranslatorBase {
    public static final String CQL_CONTENT_TYPE = "text/cql";
    public static final String JSON_ELM_CONTENT_TYPE = "application/elm+json";
    public static final String XML_ELM_CONTENT_TYPE = "application/elm+xml";
    public static final String SYSTEM_TYPE = "http://terminology.hl7.org/CodeSystem/library-type";
    public static final String SYSTEM_CODE = "logic-library";

    @Value("${mat-fhir-base}")
    private String matFhirBaseUrl;

    private final HapiFhirServer hapiServer;
    private final CQLAntlrUtils cqlAntlrUtils;
    private final MeasureExportRepository measureExportRepo;
    private final CqlLibraryRepository libRepo;
    private final LibraryCqlVisitorFactory libCqlVisitorFactory;


    public LibraryTranslator(HapiFhirServer hapiServer,
                             CQLAntlrUtils cqlAntlrUtils,
                             CqlLibraryRepository libRepo,
                             MeasureExportRepository measureExportRepo,
                             LibraryCqlVisitorFactory libCqlVisitorFactory) {
        this.hapiServer = hapiServer;
        this.cqlAntlrUtils = cqlAntlrUtils;
        this.libRepo = libRepo;
        this.measureExportRepo = measureExportRepo;
        this.libCqlVisitorFactory = libCqlVisitorFactory;
    }

    public Library translateToFhir(String libId, String cql, String elmXml, String elmJson) {
        //Used to parse the CQL and get various values.
        CqlLibrary lib = libRepo.getCqlLibraryById(libId);

        var visitor = libCqlVisitorFactory.visit(cql);
        Library result = new Library();
        result.setId(libId);
        result.setLanguage("en");
        result.setName(visitor.getName());
        result.setVersion(visitor.getVersion());
        result.setDate(new Date());
        result.setStatus(Enumerations.PublicationStatus.ACTIVE);
        result.setPublisher(lib.getSteward() != null && StringUtils.isNotBlank(lib.getSteward().getOrgName()) ?
                lib.getSteward().getOrgName() :
                "UNKNOWN");
        result.setDescription(StringUtils.defaultString(lib.getDescription(), "UNKNOWN"));
        result.setExperimental(lib.isExperimental());
        result.setContent(createContent(elmJson, cql, elmXml));
        result.setType(createType(SYSTEM_TYPE, SYSTEM_CODE));
        result.setUrl(matFhirBaseUrl + "/Library/" + lib.getCqlName());
        result.setDataRequirement(distinctDataRequirements(visitor.getDataRequirements()));
        result.setRelatedArtifact(distinctArtifacts(visitor.getRelatedArtifacts()));
        result.setText(findHumanReadable(lib.getMeasureId())); //Eventually this will be changed to liquid scripts.
        result.setMeta(createLibraryMeta());

        //When we get to publishable profile we will need to add in these:
        //result.setApprovalDate(new Date()); //TO DO: fix this.

        return result;
    }

    private Meta createLibraryMeta() {
        //Currently only one profile is allowed, but Bryn is under the impression multiples should work.
        //For now it is just exectauble until we resolve this.
        return new Meta()
                //.addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-library-cqfm")
                .addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/executable-library-cqfm");
    }

    private Narrative findHumanReadable(String measureId) {
        Narrative result = null;

        if (StringUtils.isNotBlank(measureId)) {
            var exportOpt = measureExportRepo.findByMeasureId(measureId);
            if (exportOpt.isPresent()) {
                var export = exportOpt.get();
                if (export.getHumanReadable() != null) {
                    result = createNarrative(measureId, export.getHumanReadable());
                }
            }
        }
        return result;
    }

    /**
     * @param elmJson elmJson String
     * @param cql     cql String
     * @param elmXml  elmXml String
     * @return The content element.
     */
    private List<Attachment> createContent(String elmJson, String cql, String elmXml) {
        List<Attachment> attachments = new ArrayList<>(2);
        attachments.add(createAttachment(CQL_CONTENT_TYPE, cql.getBytes()));
        attachments.add(createAttachment(XML_ELM_CONTENT_TYPE, elmXml.getBytes()));
        attachments.add(createAttachment(JSON_ELM_CONTENT_TYPE, elmJson.getBytes()));
        return attachments;
    }

    private List<RelatedArtifact> distinctArtifacts(List<RelatedArtifact> artifacts) {
        List<RelatedArtifact> result = new ArrayList<>(artifacts.size());
        //Remove duplicates. I wish HapiFhir implemented equals. Today it is SadFhir for me.
        artifacts.forEach(a -> {
            if (result.stream().noneMatch(ar -> Objects.deepEquals(a, ar))) {
                result.add(a);
            }
        });
        result.sort((ra1,ra2) -> ra1.getUrl().compareTo(ra2.getUrl()));
        return result;
    }

    private List<DataRequirement> distinctDataRequirements(List<DataRequirement> reqs) {
        List<DataRequirement> result = new ArrayList<>(reqs.size());
        //Remove duplicates. I wish HapiFhir implemented equals. Today it is SadFhir for me.
        //Object.deepEquals doesn't work at all on codeFilter for some reason.
        reqs.forEach(r -> {
            if (result.stream().noneMatch(rr -> StringUtils.equals(r.getType(), rr.getType()) &&
                    ((CollectionUtils.isEmpty(r.getCodeFilter()) && CollectionUtils.isEmpty(rr.getCodeFilter())) ||
                            (r.getCodeFilter().size() == rr.getCodeFilter().size() &&
                                    StringUtils.equals(r.getCodeFilter().get(0).getValueSet(), rr.getCodeFilter().get(0).getValueSet()) &&
                                    StringUtils.equals(r.getCodeFilter().get(0).getPath(), rr.getCodeFilter().get(0).getPath()))))) {
                result.add(r);
            }
        });
        return result;
    }
}
