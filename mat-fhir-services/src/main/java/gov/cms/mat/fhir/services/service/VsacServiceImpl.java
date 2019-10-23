package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.config.VsacConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vsac.VSACGroovyClient;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class VsacServiceImpl implements VsacService {
    private final VsacConfig vsacConfig;

    @Value("#{environment.VSAC_USER}")
    private String userName;
    @Value("#{environment.VSAC_PASS}")
    private String password;

    private VSACGroovyClient vGroovyClient;

    public VsacServiceImpl(VsacConfig vsacConfig) {
        this.vsacConfig = vsacConfig;
    }

    @PostConstruct
    public void init() {
        vGroovyClient = new VSACGroovyClient(vsacConfig.getProxyHost(),
                vsacConfig.getProxyPort(),
                vsacConfig.getServer(),
                vsacConfig.getService(),
                vsacConfig.getRetrieveMultiOidsService(),
                vsacConfig.getProfileService(), vsacConfig.getVersionService(),
                vsacConfig.getVsacServerDrcUrl());
    }

    public String validateUser() {
        String eightHourTicketForUser = vGroovyClient.getTicketGrantingTicket(userName, password);
        log.debug("eightHourTicketForUser: {}", eightHourTicketForUser);

        return eightHourTicketForUser;
    }
}
