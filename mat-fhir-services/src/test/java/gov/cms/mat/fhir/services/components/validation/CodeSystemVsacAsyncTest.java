package gov.cms.mat.fhir.services.components.validation;


import gov.cms.mat.vsac.VsacService;
import gov.cms.mat.vsac.model.CodeSystemVersionResponse;
import gov.cms.mat.vsac.model.VsacCode;
import mat.model.cql.CQLCode;
import mat.model.cql.VsacStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeSystemVsacAsyncTest {
    private static final String TOKEN = "token";
    private static final String API_KEY = "api-key";
    private static final String LOINC = "LOINC";
    private static final String CODE = "CODE-OID";
    private static final String VERSION = "1.234";

    private CQLCode cqlCode;
    private  String path;

    @InjectMocks
    private CodeSystemVsacAsync codeSystemVsacAsync;
    @Mock
    private VsacService vsacService;

    @BeforeEach
    void setUp() {
        cqlCode = new CQLCode();
        cqlCode.setCodeIdentifier("CODE:/CodeSystem/" + LOINC + "/Version/" + VERSION + "/Code/" + CODE + "/Info");
        path = "/CodeSystem/" + LOINC + "/Version/" + VERSION + "/Code/" + CODE + "/Info";
    }


    @Test
    void validateCodeApiKeyInvalid() throws ExecutionException, InterruptedException {


        CodeSystemVersionResponse vsacResponse = buildCodeSystemVersionResponse(false, "Can't log in", null);

        cqlCode.setCodeSystemName(LOINC);

        when(vsacService.getCodeSystemVersionFromName(LOINC, API_KEY)).thenReturn(vsacResponse);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN, API_KEY);
        completableFuture.get();

        assertEquals("Can't log in", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.PENDING, cqlCode.obtainValidatedWithVsac());

        verifyNoMoreInteractions(vsacService);

    }

    @Test
    void validateCodeNotInVsac() throws ExecutionException, InterruptedException {

        cqlCode.setCodeSystemOID(CodeSystemVsacAsync.NOT_IN_VSAC);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN, API_KEY);
        completableFuture.get();

        assertNull(cqlCode.getErrorMessage());

        verifyNoInteractions(vsacService);
    }


    @Test
    void validateCodeNotFound() throws ExecutionException, InterruptedException {

        CodeSystemVersionResponse vsacResponse =
                buildCodeSystemVersionResponse(false, "CodeSystem not found.", null);

        cqlCode.setCodeSystemName(LOINC);

        when(vsacService.getCodeSystemVersionFromName(LOINC, API_KEY)).thenReturn(vsacResponse);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN, API_KEY);
        completableFuture.get();

        assertEquals("Code system name: LOINC not found in UMLS! Please verify the code system name.", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());
    }


    @Test
    void validateCodeVsacCodeSystemValidatorExceptionThrown() throws ExecutionException, InterruptedException {
        cqlCode.setCodeIdentifier("http://areyou.goofy.com");

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN, API_KEY);
        completableFuture.get();

        assertEquals("Code system name: null not found in UMLS! Please verify the code system name.", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());
    }

    @Test
    void validateCodeExceptionThrown() throws ExecutionException, InterruptedException {
        cqlCode.setCodeSystemName(LOINC);
        cqlCode.setCodeOID(CODE);

        when(vsacService.getCodeSystemVersionFromName(LOINC, API_KEY)).thenThrow(new IllegalArgumentException("oops"));

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN, API_KEY);
        completableFuture.get();

        assertEquals("Code system not found in VSAC.", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());
        assertNull(cqlCode.getErrorCode());
    }


    @Test
    void validateCodeVsacCodeSystemSuccess() throws ExecutionException, InterruptedException {
        cqlCode.setCodeSystemName(LOINC);
        cqlCode.setCodeOID(CODE);

        CodeSystemVersionResponse vsacResponse = buildCodeSystemVersionResponse(true, null, VERSION);

        when(vsacService.getCodeSystemVersionFromName(LOINC, API_KEY)).thenReturn(vsacResponse);

        VsacCode vsacCode = createVsacCode("ok", null);

        when(vsacService.getCode(path, API_KEY)).thenReturn(vsacCode);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN, API_KEY);
        completableFuture.get();

        assertEquals("CODE:" + path, cqlCode.getCodeIdentifier());
        assertNull(cqlCode.getErrorMessage());
        assertEquals(VsacStatus.VALID, cqlCode.obtainValidatedWithVsac());
    }

    @Test
    void validateCodeDoesNotNeedVersion() throws ExecutionException, InterruptedException {
        cqlCode.setCodeSystemName(LOINC);
        cqlCode.setCodeOID(CODE);
        cqlCode.setCodeSystemVersionUri(VERSION);

        VsacCode vsacCode = createVsacCode("ok", null);
        when(vsacService.getCode(path, API_KEY)).thenReturn(vsacCode);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN, API_KEY);
        completableFuture.get();

        assertEquals("CODE:" + path, cqlCode.getCodeIdentifier());
        assertNull(cqlCode.getErrorMessage());
        assertEquals(VsacStatus.VALID, cqlCode.obtainValidatedWithVsac());
    }

    @Test
    void validateCodeVsacInvalidVsacStatusNoErrorsReported() throws ExecutionException, InterruptedException {
        cqlCode.setCodeSystemName(LOINC);
        cqlCode.setCodeOID(CODE);

        CodeSystemVersionResponse vsacResponse = buildCodeSystemVersionResponse(true, null, VERSION);

        when(vsacService.getCodeSystemVersionFromName(LOINC, API_KEY)).thenReturn(vsacResponse);

        VsacCode vsacCode = createVsacCode("awful", "It is awful");
        when(vsacService.getCode(path, API_KEY)).thenReturn(vsacCode);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN, API_KEY);
        completableFuture.get();

        assertEquals("CODE:" + path, cqlCode.getCodeIdentifier());
        assertEquals("It is awful", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());
        assertNull(cqlCode.getErrorCode());
    }

    @Test
    void validateCodeVsacInvalidVsacStatusErrorsReported() throws ExecutionException, InterruptedException {
        cqlCode.setCodeSystemName(LOINC);
        cqlCode.setCodeOID(CODE);

        CodeSystemVersionResponse vsacResponse = buildCodeSystemVersionResponse(true, null, VERSION);

        when(vsacService.getCodeSystemVersionFromName(LOINC, API_KEY)).thenReturn(vsacResponse);

        VsacCode vsacCode = createVsacCode("awful", "It is awful");
        vsacCode.setErrors(new VsacCode.VsacError());
        vsacCode.getErrors().setResultSet(createErrors());

        when(vsacService.getCode(path, API_KEY)).thenReturn(vsacCode);

        CompletableFuture<Void> completableFuture = codeSystemVsacAsync.validateCode(cqlCode, TOKEN, API_KEY);
        completableFuture.get();

        assertEquals("CODE:" + path, cqlCode.getCodeIdentifier());
        assertEquals("Danger Danger, Wil Robinson, It's a Twister", cqlCode.getErrorMessage());
        assertEquals(VsacStatus.IN_VALID, cqlCode.obtainValidatedWithVsac());
        assertEquals("FirstCode", cqlCode.getErrorCode());
    }

    private List<VsacCode.VsacErrorResultSet> createErrors() {
        VsacCode.VsacErrorResultSet vsacError1 = new VsacCode.VsacErrorResultSet();
        vsacError1.setErrCode("FirstCode");
        vsacError1.setErrDesc("Danger Danger, Wil Robinson");

        VsacCode.VsacErrorResultSet vsacError2 = new VsacCode.VsacErrorResultSet();
        vsacError2.setErrCode("SecondCode");
        vsacError2.setErrDesc("It's a Twister");

        return List.of(vsacError1, vsacError2);
    }

    private CodeSystemVersionResponse buildCodeSystemVersionResponse(boolean success,
                                                                     @Nullable String message,
                                                                     @Nullable String version) {
        return CodeSystemVersionResponse.builder()
                .success(success)
                .message(message)
                .version(version)
                .build();
    }

    private VsacCode createVsacCode(@Nonnull String status, @Nullable String message) {
        VsacCode vsacCode = new VsacCode();

        vsacCode.setStatus(status);
        vsacCode.setMessage(message);
        vsacCode.setData(createVsacData());
        return vsacCode;
    }

    private VsacCode.VsacData createVsacData() {
        return new VsacCode.VsacData();
    }
}