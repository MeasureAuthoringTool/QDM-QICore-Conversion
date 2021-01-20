import gov.cms.mat.vsac.RefreshTokenManagerImpl;
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
    private static final String TICKET_BASE_URL = "https://utslogin.nlm.nih.gov/cas/v1";
    private static final String VSAC_BASE_URL = "https://vsac.nlm.nih.gov";
    private final VsacService vsacService;

    public IntegrationTest() {
        vsacService = new VsacService(TICKET_BASE_URL, VSAC_BASE_URL, new RestTemplate(), RefreshTokenManagerImpl.getInstance());
    }

    public static void check(boolean b) {
        if (!b) {
            throw new RuntimeException("Assertion failed.");
        }
    }

    public static void main(String[] args) {
        IntegrationTest t = new IntegrationTest();
        log.info("Initialized");
        String tgt = t.testTicketGrantingTicket();
        t.testServiceTicket(tgt);
        t.testGetValueSet(tgt);
        t.testGetValueSetWrapper(tgt);
        t.testGetCodeSystemFromName(tgt);
        t.testGetCodeSystem(tgt);
        t.testGetProfileList(tgt);
        t.testReteriveVersionListForOid(tgt);


        // t.testGetMultipleValueSetsResponseByOID(tgt); mike commented out



        // t.testGetMultipleValueSetsResponseByOIDAndVersion(tgt);
        // t.testGetMultipleValueSetsResponseByOIDAndEffectiveDate(tgt);

        // t.testGetMultipleValueSetsResponseByOIDAndProfile(tgt); mike commented out

        log.info("Complete");
    }

    public String testTicketGrantingTicket() {
        String result = vsacService.getTicketGrantingTicket(API_KEY);
        check(result != null);
        log.info("testServiceTicket=" + result);
        return result;
    }

    public void testServiceTicket(String tgt) {
        String result = vsacService.getServiceTicket(tgt, API_KEY);
        check(result != null);
        log.info("testTicketGrantingTicket=" + result);
    }

    public void testGetValueSet(String tgt) {
        ValueSetResult result = vsacService.getValueSetResult("2.16.840.1.113883.3.117.1.7.1.201", tgt, API_KEY);
        log.info("ValueSetResult=" + result);
        check(!result.isFailResponse());
    }

    public void testGetValueSetWrapper(String tgt) {
        ValueSetWrapper result = vsacService.getVSACValueSetWrapper("2.16.840.1.113883.3.117.1.7.1.201", tgt, API_KEY);
        log.info("ValueSetWrapper=" + result);
        check(result.getVsacValueSetList() != null);
    }

    public void testGetCodeSystemFromName(String tgt) {
        CodeSystemVersionResponse result = vsacService.getCodeSystemVersionFromName("ActMood", tgt, API_KEY);
        log.info("CodeSystemVersionResponse=" + result);
        check(result.getSuccess());

    }

    public void testGetCodeSystem(String tgt) {
        VsacCode result = vsacService.getCode(
                "/CodeSystem/ActMood/Version/HL7V3.0_2019-12/Code/_ActMoodActRequest/Info", tgt, API_KEY);
        log.info("testGetCodeSystem=" + result);
        check(result.getErrors() == null);
    }

    public void testGetProfileList(String tgt) {
        BasicResponse result = vsacService.getProfileList(tgt, API_KEY);
        log.info("testGetProfileList=" + result);
        check(!result.isFailResponse());
    }

    public void testReteriveVersionListForOid(String tgt) {
        BasicResponse result = vsacService.reteriveVersionListForOid("2.16.840.1.113883.3.117.1.7.1.201", tgt);
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
                "2.16.840.1.113883.3.117.1.7.1.201", "eCQM Update 2019-05-10", tgt);
        log.info("testGetMultipleValueSetsResponseByOIDAndProfile=" + result);
        check(!result.isFailResponse());
    }
}
