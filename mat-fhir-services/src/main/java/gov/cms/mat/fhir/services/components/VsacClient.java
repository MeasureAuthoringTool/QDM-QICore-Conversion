package gov.cms.mat.fhir.services.components;

import gov.cms.mat.fhir.services.config.VsacConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.vsac.VSACGroovyClient;
import org.vsac.VSACResponseResult;

@Component
@Slf4j
public class VsacClient {
    @Getter
    private final VSACGroovyClient vGroovyClient;

    public VsacClient(VsacConfig vsacConfig) {
        vGroovyClient = new VSACGroovyClient(vsacConfig.getProxyHost(),
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

    public VSACResponseResult getData(String oid, String version, String serviceTicket) {
        // this is what mat has by profile
        // mat.qdm.default.expansion.id=Most Recent Code System Versions in VSAC

        VSACResponseResult vsacResponseResult =
             vGroovyClient.getMultipleValueSetsResponseByOIDAndVersion(oid, version, serviceTicket);

      //  VSACResponseResult vsacResponseResult = vGroovyClient.getProfileList(serviceTicket); // does work


        log.debug("vsacResponseResult: {}", vsacResponseResult);

        return vsacResponseResult;
    }
}
