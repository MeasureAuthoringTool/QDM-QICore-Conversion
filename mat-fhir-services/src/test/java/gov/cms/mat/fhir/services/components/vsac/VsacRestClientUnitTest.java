package gov.cms.mat.fhir.services.components.vsac;


import gov.cms.mat.fhir.services.ResourceFileUtil;
import gov.cms.mat.fhir.services.config.VsacConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VsacRestClientUnitTest implements ResourceFileUtil {
    private static final String BASE_URL = "https://vsac.nlm.nih.gov";
    private static final String TICKET_URL = BASE_URL + "/vsac/ws/Ticket";

    private static final String GRANT_TICKET = "grantingTicket";
    private static final String SINGLE_USE_TICKET = "I_expire_after_five_minutes_and_can_be_used_once";

    private static final String CODE = "/CodeSystem/LOINC/Version/2.67/Code/21112-8/Info";

    @Mock
    private VsacConfig vsacConfig;
    @InjectMocks
    private VsacRestClient vsacRestClient;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        when(vsacConfig.getService()).thenReturn(BASE_URL);
    }

    @Test
    void fetchGrantingTicketOK() {
        when(restTemplate.exchange(ArgumentMatchers.eq(TICKET_URL),
                ArgumentMatchers.eq(HttpMethod.POST),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<>(GRANT_TICKET, HttpStatus.OK));


        String grantingTicket = vsacRestClient.fetchGrantingTicket("username", "password");
        assertEquals(GRANT_TICKET, grantingTicket);

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }

    @Test
    void fetchGrantingTicketError() {
        when(restTemplate.exchange(ArgumentMatchers.eq(TICKET_URL),
                ArgumentMatchers.eq(HttpMethod.POST),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<>("", HttpStatus.UNPROCESSABLE_ENTITY));


        String grantingTicket = vsacRestClient.fetchGrantingTicket("username", "password");
        assertNull(grantingTicket);

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }

    @Test
    void fetchGrantingTicketException() {
        when(restTemplate.exchange(ArgumentMatchers.eq(TICKET_URL),
                ArgumentMatchers.eq(HttpMethod.POST),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<String>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "oops"));


        String grantingTicket = vsacRestClient.fetchGrantingTicket("username", "password");
        assertNull(grantingTicket);

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }

    @Test
    void fetchSingleUseTicketOK() {
        mockFetchingSingleUse();


        String singleUseTicket = vsacRestClient.fetchSingleUseTicket(GRANT_TICKET);
        assertEquals(SINGLE_USE_TICKET, singleUseTicket);

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }

    private void mockFetchingSingleUse() {
        when(restTemplate.exchange(ArgumentMatchers.eq(TICKET_URL + "/" + GRANT_TICKET),
                ArgumentMatchers.eq(HttpMethod.POST),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<>(SINGLE_USE_TICKET, HttpStatus.OK));
    }

    @Test
    void fetchCodeSystemCannotGetSingleUseTicket() {
        when(restTemplate.exchange(ArgumentMatchers.eq(TICKET_URL + "/" + GRANT_TICKET),
                ArgumentMatchers.eq(HttpMethod.POST),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<String>>any()))
                .thenReturn(new ResponseEntity<>("Wipe-out", HttpStatus.INTERNAL_SERVER_ERROR));

        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem(CODE, GRANT_TICKET);

        assertEquals("error", vsacResponse.getStatus());
        assertEquals("Cannot obtain a single-use service ticket.", vsacResponse.getMessage());

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }

    @Test
    void fetchCodeSystemOK() {
        mockFetchingSingleUse();

        VsacResponse vsacResponseResult = new VsacResponse();
        vsacResponseResult.setStatus("A-OK");
        vsacResponseResult.setMessage("Howdy");

        when(restTemplate.getForEntity(ArgumentMatchers.any(),
                ArgumentMatchers.<Class<VsacResponse>>any()))
                .thenReturn(new ResponseEntity<>(vsacResponseResult, HttpStatus.OK));

        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem(CODE, GRANT_TICKET);

        assertEquals("A-OK", vsacResponse.getStatus());
        assertEquals("Howdy", vsacResponse.getMessage());

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }

    @Test
    void fetchCodeSystemError() {
        mockFetchingSingleUse();

        VsacResponse vsacResponseResult = new VsacResponse();
        vsacResponseResult.setStatus("error");
        vsacResponseResult.setMessage("No Code found");

        when(restTemplate.getForEntity(ArgumentMatchers.any(),
                ArgumentMatchers.<Class<VsacResponse>>any()))
                .thenReturn(new ResponseEntity<>(vsacResponseResult, HttpStatus.BAD_REQUEST));

        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem(CODE, GRANT_TICKET);

        assertEquals("error", vsacResponse.getStatus());
        assertEquals("No Code found", vsacResponse.getMessage());

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }

    @Test
    void fetchCodeSystemErrorNoResponseBody() {
        mockFetchingSingleUse();

        when(restTemplate.getForEntity(ArgumentMatchers.any(),
                ArgumentMatchers.<Class<VsacResponse>>any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem(CODE, GRANT_TICKET);

        assertEquals("error", vsacResponse.getStatus());
        assertEquals(HttpStatus.BAD_REQUEST.toString(), vsacResponse.getMessage());

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }

    @Test
    void fetchCodeSystemException() {
        mockFetchingSingleUse();

        when(restTemplate.getForEntity(ArgumentMatchers.any(),
                ArgumentMatchers.<Class<VsacResponse>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem(CODE, GRANT_TICKET);

        assertEquals("error", vsacResponse.getStatus());
        assertEquals("BAD_REQUEST", vsacResponse.getMessage()); //no body or status only have HttpStatus

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }


    @Test
    void fetchCodeSystemExceptionWithStatusAndError() {
        mockFetchingSingleUse();

        when(restTemplate.getForEntity(ArgumentMatchers.any(),
                ArgumentMatchers.<Class<VsacResponse>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "oops"));

        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem(CODE, GRANT_TICKET);

        assertEquals("error", vsacResponse.getStatus());
        assertEquals("oops", vsacResponse.getMessage()); //no body in exception get from statusText

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }

    @Test
    void fetchCodeSystemExceptionWithStatusAndErrorAndBody() {
        mockFetchingSingleUse();

        byte[] body = getStringFromResource("/vsacResponseError.json").getBytes();

        when(restTemplate.getForEntity(ArgumentMatchers.any(),
                ArgumentMatchers.<Class<VsacResponse>>any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "oops", body, StandardCharsets.UTF_8));

        VsacResponse vsacResponse = vsacRestClient.fetchCodeSystem(CODE, GRANT_TICKET);

        assertEquals("error", vsacResponse.getStatus());
        // have body in exception get from that ignoring statusText
        assertEquals("Errors getting code...", vsacResponse.getMessage());

        verifyNoMoreInteractions(vsacConfig, restTemplate);
    }
}