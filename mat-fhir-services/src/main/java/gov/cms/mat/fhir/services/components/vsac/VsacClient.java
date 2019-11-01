package gov.cms.mat.fhir.services.components.vsac;

import gov.cms.mat.fhir.services.config.VsacConfig;
import gov.cms.mat.vsac.VsacRestClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.vsac.VSACResponseResult;

@Component
@Slf4j
/**
 * Supporting vsac api documents
 * https://www.nlm.nih.gov/vsac/support/usingvsac/vsacsvsapiv2.html
 */
public class VsacClient {
    // this is what mat has by profile -- mat.qdm.default.expansion.id=Most Recent Code System Versions in VSAC
    private static final String PROFILE = "Most Recent Code System Versions in VSAC";

    @Getter
    private final VsacRestClient vGroovyClient;

    public VsacClient(VsacConfig vsacConfig) {
        vGroovyClient = new VsacRestClient(vsacConfig.getProxyHost(),
                vsacConfig.getProxyPort(),
                vsacConfig.getServer(),
                vsacConfig.getService(),
                vsacConfig.getRetrieveMultiOidsService(),
                vsacConfig.getProfileService(),
                vsacConfig.getVersionService(),
                vsacConfig.getVsacServerDrcUrl());
    }

    public String getGrantingTicket(String userName, String password) {
        return vGroovyClient.getTicketGrantingTicket(userName, password);
    }

    public String getServiceTicket(String grantingTicket) {
        return vGroovyClient.getServiceTicket(grantingTicket);
    }

    public VSACResponseResult getDataFromProfile(String oid, String serviceTicket) {
        VSACResponseResult vsacResponseResult = vGroovyClient.getVsacDataForConversion(oid, serviceTicket, PROFILE);

        if( vsacResponseResult.isIsFailResponse()) {
            log.debug("vsacResponseResult failed with reason: {}", vsacResponseResult.getFailReason());
        }

        return vsacResponseResult;
    }
}
