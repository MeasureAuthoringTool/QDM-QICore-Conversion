package gov.cms.mat.fhir.services.translate;

import mat.client.measure.ManageCompositeMeasureDetailModel;
import mat.client.measure.PeriodModel;
import org.hl7.fhir.r4.model.Measure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeasureTranslatorTest {

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

        measureTranslator = new MeasureTranslator(compositeModel, humanReadable, baseURL);
        
        
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
}
