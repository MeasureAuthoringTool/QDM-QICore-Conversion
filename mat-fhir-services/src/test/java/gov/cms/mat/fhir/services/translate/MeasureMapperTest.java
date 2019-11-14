/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.translate;

import java.text.SimpleDateFormat;
import java.util.Date;
import mat.client.measure.ManageCompositeMeasureDetailModel;
import org.hl7.fhir.r4.model.Measure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import gov.cms.mat.fhir.services.translate.MeasureMapper;
import java.util.ArrayList;
import java.util.List;
import mat.client.measure.PeriodModel;

/**
 *
 * @author duanedecouteau
 */
class MeasureMapperTest {
    
    private ManageCompositeMeasureDetailModel compositeModel;
    private String humanReadable;
    private String baseURL;
    private String measureURL;
    private MeasureMapper measureMapper;
    
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
        
        measureMapper = new MeasureMapper(compositeModel, humanReadable, baseURL);
        
        
    }
    
    @Test
    void testTranslateToFhir_MeasureIdentity() {
        
        Measure fhirMeasure = measureMapper.translateToFhir();
        
        assertEquals("402803826529d99f0165d33515622e23", fhirMeasure.getId());
        assertEquals("Hospital ReAdmits", fhirMeasure.getName());
        assertEquals("0.0001", fhirMeasure.getMeta().getVersionId());
        
        assertEquals(measureURL, fhirMeasure.getUrl());
        
    }
    
    @Test
    void testTranslateToFhir_MeasureClinicalGuidance() {
        Measure fhirMeasure = measureMapper.translateToFhir();

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
