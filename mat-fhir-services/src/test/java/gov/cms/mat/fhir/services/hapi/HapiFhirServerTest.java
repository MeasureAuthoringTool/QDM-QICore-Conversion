package gov.cms.mat.fhir.services.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SearchTotalModeEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IDelete;
import ca.uhn.fhir.rest.gclient.IDeleteTyped;
import ca.uhn.fhir.rest.gclient.IGetPage;
import ca.uhn.fhir.rest.gclient.IGetPageTyped;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.ITransaction;
import ca.uhn.fhir.rest.gclient.ITransactionTyped;
import ca.uhn.fhir.rest.gclient.IUntypedQuery;
import gov.cms.mat.fhir.services.config.security.SecurityFilter;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HapiFhirServerTest {
    private static final String MAT_API_KEY = "SECRET";

    @Mock
    IGenericClient hapiClient;
    @Mock
    ITransactionTyped<Bundle> bundleITransactionTyped;
    @Mock
    IUntypedQuery iUntypedQuery;
    @Mock
    IQuery iQuery;
    @Mock
    private FhirContext ctx;
    @Mock
    private ITransaction iTransaction;
    @InjectMocks
    private HapiFhirServer hapiFhirServer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hapiFhirServer, "baseURL", "http://acme.com");
        ReflectionTestUtils.setField(hapiFhirServer, "hapiClient", hapiClient);

        ReflectionTestUtils.setField(hapiFhirServer, "matApiKey", MAT_API_KEY);
    }

    @Test
    void getCtx() {
        assertEquals(ctx, hapiFhirServer.getCtx());
    }

    @Test
    void getHapiClient() {
        assertEquals(hapiClient, hapiFhirServer.getHapiClient());
    }

    @Test
    void delete() {
        ValueSet resource = new ValueSet();
        IDelete iDelete = mock(IDelete.class);
        IDeleteTyped iDeleteTyped = mock(IDeleteTyped.class);

        MethodOutcome iBaseOperationOutcomeToReturn = mock(MethodOutcome.class);

        when(hapiClient.delete()).thenReturn(iDelete);
        when(iDelete.resource(resource)).thenReturn(iDeleteTyped);
        when(iDeleteTyped.prettyPrint()).thenReturn(iDeleteTyped);
        when(iDeleteTyped.encodedJson()).thenReturn(iDeleteTyped);
        when(iDeleteTyped.withAdditionalHeader(SecurityFilter.MAT_API_KEY, MAT_API_KEY)).thenReturn(iDeleteTyped);
        when(iDeleteTyped.execute()).thenReturn(iBaseOperationOutcomeToReturn);

        MethodOutcome iBaseOperationOutcomeReturned = hapiFhirServer.delete(resource);
        assertEquals(iBaseOperationOutcomeToReturn, iBaseOperationOutcomeReturned);

        verify(hapiClient).delete();
        verify(iDelete).resource(resource);
        verify(iDeleteTyped).prettyPrint();
        verify(iDeleteTyped).encodedJson();
        verify(iDeleteTyped).execute();
    }

    @Test
    void isValueSetInHapi() {
        Bundle toReturn = new Bundle();


        when(hapiClient.search()).thenReturn(iUntypedQuery);
        when(iUntypedQuery.forResource(ValueSet.class)).thenReturn(iQuery);
        when(iQuery.where(any(ICriterion.class))).thenReturn(iQuery);
        when(iQuery.returnBundle(Bundle.class)).thenReturn(iQuery);
        when(iQuery.withAdditionalHeader("Cache-Control", "no-cache")).thenReturn(iQuery);
        when(iQuery.withAdditionalHeader(SecurityFilter.MAT_API_KEY, MAT_API_KEY)).thenReturn(iQuery);
        when(iQuery.execute()).thenReturn(toReturn);

        Bundle returned = hapiFhirServer.getValueSetBundle("OID");
        assertEquals(toReturn, returned);

        verify(hapiClient).search();
        verify(iUntypedQuery).forResource(ValueSet.class);
        verify(iQuery).where(any(ICriterion.class));
        verify(iQuery).returnBundle(Bundle.class);
        verify(iQuery).execute();
    }

    @Test
    void count() {
        Bundle toReturn = new Bundle();
        toReturn.setTotal(Integer.MAX_VALUE);

        when(hapiClient.search()).thenReturn(iUntypedQuery);
        when(iUntypedQuery.forResource(ValueSet.class)).thenReturn(iQuery);
        when(iQuery.totalMode(SearchTotalModeEnum.ACCURATE)).thenReturn(iQuery);
        when(iQuery.returnBundle(Bundle.class)).thenReturn(iQuery);
        when(iQuery.withAdditionalHeader("Cache-Control", "no-cache")).thenReturn(iQuery);
        when(iQuery.withAdditionalHeader(SecurityFilter.MAT_API_KEY, MAT_API_KEY)).thenReturn(iQuery);
        when(iQuery.execute()).thenReturn(toReturn);

        assertEquals(Integer.MAX_VALUE, hapiFhirServer.count(ValueSet.class));

        verify(hapiClient).search();
        verify(iUntypedQuery).forResource(ValueSet.class);
        verify(iQuery).totalMode(SearchTotalModeEnum.ACCURATE);
        verify(iQuery).returnBundle(Bundle.class);
        verify(iQuery).execute();
    }

    @Test
    void getAll() {
        Bundle toReturn = new Bundle();

        when(hapiClient.search()).thenReturn(iUntypedQuery);
        when(iUntypedQuery.forResource(ValueSet.class)).thenReturn(iQuery);
        when(iQuery.returnBundle(Bundle.class)).thenReturn(iQuery);
        when(iQuery.withAdditionalHeader("Cache-Control", "no-cache")).thenReturn(iQuery);
        when(iQuery.withAdditionalHeader(SecurityFilter.MAT_API_KEY, MAT_API_KEY)).thenReturn(iQuery);
        when(iQuery.execute()).thenReturn(toReturn);

        Bundle returned = hapiFhirServer.getAll(ValueSet.class);
        assertEquals(toReturn, returned);

        verify(hapiClient).search();
        verify(iUntypedQuery).forResource(ValueSet.class);
        verify(iQuery).returnBundle(Bundle.class);
        verify(iQuery).execute();
    }

    @Test
    void getNextPage() {
        Bundle param = new Bundle();
        Bundle toReturn = new Bundle();

        IGetPage iGetPage = mock(IGetPage.class);
        IGetPageTyped iBaseBundle = mock(IGetPageTyped.class);

        when(hapiClient.loadPage()).thenReturn(iGetPage);
        when(iGetPage.next(param)).thenReturn(iBaseBundle);
        when(iBaseBundle.withAdditionalHeader(SecurityFilter.MAT_API_KEY, MAT_API_KEY)).thenReturn(iBaseBundle);
        when(iBaseBundle.execute()).thenReturn(toReturn);

        Bundle returned = hapiFhirServer.getNextPage(param);

        assertEquals(toReturn, returned);

        verify(hapiClient).loadPage();
        verify(iGetPage).next(param);
        verify(iBaseBundle).execute();
    }
}