package gov.cms.mat.fhir.services.components.vsac;

import com.google.common.annotations.VisibleForTesting;
import gov.cms.mat.fhir.services.config.VsacConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.vsac.VSACResponseResult;

@Component
@Slf4j
/*
 * Supporting vsac api documents
 * https://www.nlm.nih.gov/vsac/support/usingvsac/vsacsvsapiv2.html
 */
public class VsacClient {
    // this is what MAT has by profile -- mat.qdm.default.expansion.id=Most Recent Code System Versions in VSAC
    static final String PROFILE = "Most Recent Code System Versions in VSAC";

    private final VsacConfig vsacConfig;

    private final VsacRestClient vsacRestClient;

    public VsacClient(VsacConfig vsacConfig,
                      VsacRestClient vsacRestClient) {
        this.vsacConfig = vsacConfig;
        this.vsacRestClient = vsacRestClient;
    }

    // For testing purposes only
    @VisibleForTesting
    public String getGrantingTicket(String userName, String password) {
        return vsacConfig.getVsacRestClient().getTicketGrantingTicket(userName, password);
    }

    public String getServiceTicket(String grantingTicket) {
        return vsacRestClient.fetchSingleUseTicket(grantingTicket);
    }

    public VSACResponseResult getDataFromProfile(String oid, String serviceTicket) {
        return vsacConfig.getVsacRestClient().getVsacDataForConversion(oid, serviceTicket, PROFILE);
    }

    public VSACResponseResult getDirectReferenceCode(String codeURLString, String serviceTicket) {
        return vsacConfig.getVsacRestClient().getDirectReferenceCode(codeURLString, serviceTicket);
    }

    public VSACResponseResult getMultipleValueSetsResponseByOID(String oid, String serviceTicket, String expansionId) {
        return vsacConfig.getVsacRestClient().getMultipleValueSetsResponseByOID(oid, serviceTicket, expansionId);
    }
}
