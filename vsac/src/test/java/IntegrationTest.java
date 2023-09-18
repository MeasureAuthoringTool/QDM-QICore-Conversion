import gov.cms.mat.vsac.VsacService;
import gov.cms.mat.vsac.model.BasicResponse;
import gov.cms.mat.vsac.model.CodeSystemVersionResponse;
import gov.cms.mat.vsac.model.ValueSetResult;
import gov.cms.mat.vsac.model.ValueSetWrapper;
import gov.cms.mat.vsac.model.VsacCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class IntegrationTest {
    private static final String API_KEY = "257e01b8-392d-487c-be24-204a432c9fab";
    private static final String VSAC_BASE_URL = "https://vsac.nlm.nih.gov";
    private final VsacService vsacService;

    public IntegrationTest() {
        vsacService = new VsacService(VSAC_BASE_URL, new RestTemplate());
    }

    public static void check(boolean b) {
        if (!b) {
            throw new RuntimeException("Assertion failed.");
        }
    }

    public static void main(String[] args) {
        IntegrationTest t = new IntegrationTest();
        log.info("Initialized");
        t.testGetValueSet(API_KEY);
        t.testGetValueSetWrapper(API_KEY);
        t.testGetCodeSystemFromName(API_KEY);
        t.testGetCodeSystem(API_KEY);
        t.testGetProfileList(API_KEY);
        t.testReteriveVersionListForOid(API_KEY);


        // t.testGetMultipleValueSetsResponseByOID(tgt); mike commented out



        // t.testGetMultipleValueSetsResponseByOIDAndVersion(tgt);
        // t.testGetMultipleValueSetsResponseByOIDAndEffectiveDate(tgt);

        // t.testGetMultipleValueSetsResponseByOIDAndProfile(tgt); mike commented out

        log.info("Complete");
    }

    public void testGetValueSet(String apiKey) {
    		ValueSetResult result = vsacService.getValueSetResult("2.16.840.1.113883.3.117.1.7.1.201", apiKey);
        log.info("ValueSetResult=" + result);
        check(!result.isFailResponse());
    }

    public void testGetValueSetWrapper(String apiKey) {
    		ValueSetWrapper result = vsacService.getVSACValueSetWrapper("2.16.840.1.113883.3.117.1.7.1.201", apiKey);
        log.info("ValueSetWrapper=" + result);
        check(result.getVsacValueSetList() != null);
    }

    public void testGetCodeSystemFromName(String apiKey) {
    		CodeSystemVersionResponse result = vsacService.getCodeSystemVersionFromName("ActMood", apiKey);
        log.info("CodeSystemVersionResponse=" + result);
        check(result.getSuccess());

    }

    public void testGetCodeSystem(String apiKey) {
    	VsacCode result = vsacService.getCode(
          "/CodeSystem/ActMood/Version/HL7V3.0_2019-12/Code/_ActMoodActRequest/Info", apiKey);
        log.info("testGetCodeSystem=" + result);
        check(result.getErrors() == null);
    }

    public void testGetProfileList(String apiKey) {
    		BasicResponse result = vsacService.getProfileList(apiKey);
        log.info("testGetProfileList=" + result);
        check(!result.isFailResponse());
    }

    public void testReteriveVersionListForOid(String apiKey) {
    		BasicResponse result = vsacService.reteriveVersionListForOid("2.16.840.1.113883.3.117.1.7.1.201", apiKey);
        log.info("testReteriveVersionListForOid=" + result);
        check(!result.isFailResponse());
    }

    public void testGetMultipleValueSetsResponseByOID(String tgt) {
        BasicResponse result = vsacService.getMultipleValueSetsResponseByOID(
                "2.16.840.1.113883.3.117.1.7.1.201", tgt, "eCQM Update 2019-05-10");
        log.info("testGetMultipleValueSetsResponseByOID=" + result);
        check(!result.isFailResponse());
    }

    public void testGetMultipleValueSetsResponseByOIDAndVersion(String tgt) {
        //params are wrong here.
        BasicResponse result = vsacService.getMultipleValueSetsResponseByOIDAndVersion(
                "2.16.840.1.113883.3.117.1.7.1.201", "HL7V3.0_2019-12", tgt);
        log.info("testGetMultipleValueSetsResponseByOIDAndVersion=" + result);
        check(!result.isFailResponse());
    }

    public void testGetMultipleValueSetsResponseByOIDAndEffectiveDate(String tgt) {
        //params are wrong here.
        BasicResponse result = vsacService.getMultipleValueSetsResponseByOIDAndEffectiveDate(
                "2.16.840.1.113883.3.117.1.7.1.201", "2020-03-19", tgt);
        log.info("testGetMultipleValueSetsResponseByOIDAndEffectiveDate=" + result);
        check(!result.isFailResponse());
    }

    public void testGetMultipleValueSetsResponseByOIDAndProfile(String tgt) {
        BasicResponse result = vsacService.getMultipleValueSetsResponseByOIDAndProfile(
        			"2.16.840.1.113883.3.117.1.7.1.201", "eCQM Update 2019-05-10", API_KEY);
        log.info("testGetMultipleValueSetsResponseByOIDAndProfile=" + result);
        check(!result.isFailResponse());
    }
}
