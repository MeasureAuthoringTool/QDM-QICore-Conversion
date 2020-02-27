package gov.cms.mat.fhir.services.translate;

import gov.cms.mat.fhir.commons.model.MeasureDetails;
import gov.cms.mat.fhir.commons.model.MeasureDetailsReference;
import gov.cms.mat.fhir.commons.model.MeasureReferenceType;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measure.PeriodModel;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeasureTranslatorTest {

    private gov.cms.mat.fhir.commons.model.Measure matMeasure;
    private ManageCompositeMeasureDetailModel compositeModel;
    private String humanReadable;
    private String baseURL;
    private String measureURL;
    private MeasureTranslator measureTranslator;

    @BeforeEach
    void setup() {
        compositeModel = new ManageCompositeMeasureDetailModel();
        humanReadable = "<somefield> blah blah</somefield>";
        baseURL = "http://localhost:8080/hapi-fhir-jpaserver/fhir/";
        compositeModel.setCalenderYear(false);
        compositeModel.setClinicalRecomms("These are the clinical recommendations");
        compositeModel.setCompositeScoringMethod("Proportion");;
        compositeModel.setCopyright("Copyright Statement");
        compositeModel.setDefinitions("These are Defininitions");
        compositeModel.setDeleted(false);
        compositeModel.setDenominator("The denominator");
        compositeModel.setDenominatorExceptions("Denominator exceptions");
        compositeModel.setDenominatorExclusions("Denominator Exclusions");
        compositeModel.setDescription("Measure Description");
        compositeModel.setDisclaimer("Measure Disclaimer");
        compositeModel.setDraft(false);
        compositeModel.setEndorseByNQF(Boolean.TRUE);
        compositeModel.seteMeasureId(44);
        compositeModel.setEndorsement("This is the measure endorsement");
        compositeModel.setFinalizedDate(getTodaysDateString());
        compositeModel.setGuidance("This is the measures guidance");
        compositeModel.setId("402803826529d99f0165d33515622e23");
        compositeModel.setImprovNotations("This is the improvement");
        compositeModel.setIsPatientBased(true);
        compositeModel.setMeasFromPeriod(getTodaysDateString());
        compositeModel.setMeasScoring("Proportion");
        compositeModel.setMeasToPeriod(getTodaysDateString());
        compositeModel.setMeasureId("402803826529d99f0165d33515622e23");
        compositeModel.setMeasureName("Hospital ReAdmits");
        compositeModel.setMeasureOwnerId("2.16.840.1.113883.3.1275");
        compositeModel.setMeasurePopulation("This is measure population");
        compositeModel.setNqfId("9999");
        compositeModel.setNumerator("this is numerator");
        compositeModel.setNumeratorExclusions("this is numerator exclusion");
        compositeModel.setOrgVersionNumber("0.0001");
        compositeModel.setQdmVersion("v5.8");
        compositeModel.setRationale("this is rationale");
        compositeModel.setRateAggregation("this is rate aggregation");
        compositeModel.setRevisionNumber("0.0001");
        compositeModel.setRiskAdjustment("this is the risk adjustment");
        compositeModel.setScoringAbbr("Proportion");
        compositeModel.setShortName("HospAdmits");
        compositeModel.setStewardValue("cms.gov");
        compositeModel.setStratification("this is stratification");
        compositeModel.setSupplementalData("this is supplementaldata");
        compositeModel.setTransmissionFormat("zip");
        compositeModel.setVersionNumber("0.0001");
        List<String> refList = new ArrayList<>();
        compositeModel.setReferencesList(refList);
        List<mat.model.MeasureType> matTypeList = new ArrayList<>();
        compositeModel.setMeasureTypeSelectedList(matTypeList);
        PeriodModel pModel = new PeriodModel();
        pModel.setStartDate("2019-11-05 00:00:00");
        pModel.setStopDate("2020-11-05 00:00:00");
        compositeModel.setPeriodModel(pModel);
        measureURL = baseURL + "Measure/402803826529d99f0165d33515622e23";

        matMeasure = new gov.cms.mat.fhir.commons.model.Measure();
        matMeasure.setMeasureDetailsCollection(new ArrayList<>());
        matMeasure.getMeasureDetailsCollection().add(new MeasureDetails());
        matMeasure.setId(compositeModel.getId());

        measureTranslator = new MeasureTranslator(matMeasure, compositeModel, humanReadable, baseURL);
    }

    @Test
    void testReferenceTypeNotInDB() {
        String[] refs = {
                "XML Reference 1",
                "XML Reference 2",
                "XML Reference 3",
                "XML Reference 4",
                "XML Reference 5"
        };

        Arrays.stream(refs).forEach(s -> compositeModel.getReferencesList().add(s));
        Measure fhirMeasure = measureTranslator.translateToFhir();

        List<RelatedArtifact> artifacts = fhirMeasure.getRelatedArtifact();
        assertEquals(refs.length,fhirMeasure.getRelatedArtifact().size());
        for (int i = 0; i < refs.length; i++) {
            assertEquals(refs[i],artifacts.get(i).getCitation());
            assertEquals(MeasureTranslator.DEFAULT_ARTIFACT_TYPE,artifacts.get(i).getType());
        }
    }

    @Test
    void testReferenceTypeInDB() {
        List<MeasureDetailsReference> refs = new ArrayList<>();

        MeasureDetailsReference ref1 = new MeasureDetailsReference();
        ref1.setReference("DB Reference 1");
        ref1.setReferenceType(MeasureReferenceType.CITATION);
        ref1.setReferenceNumber(0);
        refs.add(ref1);

        MeasureDetailsReference ref2 = new MeasureDetailsReference();
        ref2.setReference("DB Reference 2");
        ref2.setReferenceType(MeasureReferenceType.DOCUMENTATION);
        refs.add(ref2);

        MeasureDetailsReference ref3 = new MeasureDetailsReference();
        ref3.setReference("DB Reference 3");
        ref3.setReferenceType(MeasureReferenceType.JUSTIFICATION);
        refs.add(ref3);

        MeasureDetailsReference ref4 = new MeasureDetailsReference();
        ref4.setReference("DB Reference 3");
        ref4.setReferenceType(MeasureReferenceType.UNKNOWN);
        refs.add(ref4);

        getMeasureDetails().setMeasureDetailsReferenceCollection(refs);

        Measure fhirMeasure = measureTranslator.translateToFhir();

        List<RelatedArtifact> artifacts = fhirMeasure.getRelatedArtifact();

        assertEquals(refs.size(),fhirMeasure.getRelatedArtifact().size());

        assertEquals(ref1.getReference(),artifacts.get(0).getCitation());
        assertEquals(RelatedArtifact.RelatedArtifactType.CITATION,artifacts.get(0).getType());

        assertEquals(ref2.getReference(),artifacts.get(1).getCitation());
        assertEquals(RelatedArtifact.RelatedArtifactType.DOCUMENTATION,artifacts.get(1).getType());

        assertEquals(ref3.getReference(),artifacts.get(2).getCitation());
        assertEquals(RelatedArtifact.RelatedArtifactType.JUSTIFICATION,artifacts.get(2).getType());

        assertEquals(ref4.getReference(),artifacts.get(3).getCitation());
        assertEquals(MeasureTranslator.DEFAULT_ARTIFACT_TYPE,artifacts.get(3).getType());
    }

    @Test
    void testReferenceTypeDBAndXml() {
        String[] refs = {
                "XML Reference 1",
                "XML Reference 2",
                "XML Reference 3",
                "XML Reference 4",
                "XML Reference 5"
        };
        Arrays.stream(refs).forEach(s -> compositeModel.getReferencesList().add(s));

        List<MeasureDetailsReference> mdrs = new ArrayList<>();
        MeasureDetailsReference ref1 = new MeasureDetailsReference();
        ref1.setReference("DB Reference 1");
        ref1.setReferenceType(MeasureReferenceType.CITATION);
        mdrs.add(ref1);
        getMeasureDetails().setMeasureDetailsReferenceCollection(mdrs);

        Measure fhirMeasure = measureTranslator.translateToFhir();

        assertEquals(1,fhirMeasure.getRelatedArtifact().size());
        List<RelatedArtifact> artifacts = fhirMeasure.getRelatedArtifact();

        assertEquals(ref1.getReference(),artifacts.get(0).getCitation());
        assertEquals(RelatedArtifact.RelatedArtifactType.CITATION,artifacts.get(0).getType());
    }
    
    @Test
    void testTranslateToFhir_MeasureIdentity() {

        Measure fhirMeasure = measureTranslator.translateToFhir();
        
        assertEquals("402803826529d99f0165d33515622e23", fhirMeasure.getId());
        assertEquals("Hospital ReAdmits", fhirMeasure.getName());
        //  assertEquals("0.0001", fhirMeasure.getMeta().getVersionId()); TODO meta is off for now
        
        assertEquals(measureURL, fhirMeasure.getUrl());
        
    }
    
    @Test
    void testTranslateToFhir_MeasureClinicalGuidance() {
        Measure fhirMeasure = measureTranslator.translateToFhir();

        assertEquals("this is rationale", fhirMeasure.getRationale());
        assertEquals("This is the measures guidance", fhirMeasure.getGuidance());
        assertEquals("Measure Description", fhirMeasure.getDescription());
        assertEquals("These are the clinical recommendations", fhirMeasure.getClinicalRecommendationStatement());
        
    }
        
    
    private String getTodaysDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String res = "";
        try {
            Date today = new Date();
            res = sdf.format(today);
        }
        catch(Exception ex) {}
        return res;
    }

    private MeasureDetails getMeasureDetails() {
        return matMeasure.getMeasureDetailsCollection().iterator().next();
    }
}
