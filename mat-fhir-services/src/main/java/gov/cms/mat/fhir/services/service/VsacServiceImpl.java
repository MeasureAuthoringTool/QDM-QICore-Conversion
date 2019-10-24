package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.components.VsacClient;
import gov.cms.mat.fhir.services.service.support.VsacTicket;
import lombok.extern.slf4j.Slf4j;
import mat.model.cql.CQLQualityDataSetDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vsac.VSACResponseResult;

@Service
@Slf4j
public class VsacServiceImpl implements VsacService {
    private final VsacClient vsacClient;

    @Value("#{environment.VSAC_USER}")
    private String userName;
    @Value("#{environment.VSAC_PASS}")
    private String password;

    @Value("28795") // 8 hours - 5 secs
    private String grantedTicketTimeOutSeconds;
    @Value("485") // 8 hours - 5 secs
    private String serviceTicketTimeOutSeconds;


    private VsacGrantingTicket vsacGrantingTicket;
    private VsacServiceTicket vsacServiceTicket;

    public VsacServiceImpl(VsacClient vsacClient) {
        this.vsacClient = vsacClient;
    }

    public boolean validateUser() {
        if (vsacGrantingTicket == null || vsacGrantingTicket.isInValid()) {
            return getGrantingTicket();
        } else {
            return true;
        }
    }


    public boolean validateTicket() {
        if (!validateUser()) {
            return false;
        }

        if (vsacServiceTicket == null || vsacServiceTicket.isInValid()) {
            return getServiceTicket();
        } else {
            return true;
        }
    }

    @Override
    public boolean getData(CQLQualityDataSetDTO dto) {
        if (!validateTicket()) {
            return false;
        } else {
            VSACResponseResult vsacResponseResult =
                    vsacClient.getData(dto.getOid(), dto.getVersion(), vsacServiceTicket.getTicket());

            return isSuccessFull(vsacResponseResult);
        }
    }

    private boolean isSuccessFull(VSACResponseResult vsacResponseResult) {
        return vsacResponseResult != null &&
                vsacResponseResult.getXmlPayLoad() != null &&
                !vsacResponseResult.isIsFailResponse();

    }


    private synchronized boolean getServiceTicket() {
        String serviceTicket = vsacClient.getServiceTicket(vsacGrantingTicket.getTicket());

        log.debug("serviceTicket: {}", serviceTicket);

        if (serviceTicket == null) {
            vsacServiceTicket = null;
            return false;
        } else {
            vsacServiceTicket = new VsacServiceTicket(serviceTicket);
            return true;
        }
    }

    private synchronized boolean getGrantingTicket() {
        String grantingTicket = vsacClient.getGrantingTicket(userName, password);

        log.debug("grantingTicket: {}", grantingTicket);

        if (grantingTicket == null) {
            vsacGrantingTicket = null;
            return false;
        } else {
            vsacGrantingTicket = new VsacGrantingTicket(grantingTicket);
            return true;
        }
    }

    private class VsacGrantingTicket extends VsacTicket {
        private VsacGrantingTicket(String ticket) {
            super(ticket);
        }

        @Override
        protected long getTimeOutSeconds() {
            return Long.parseLong(grantedTicketTimeOutSeconds);
        }
    }

    private class VsacServiceTicket extends VsacTicket {
        private VsacServiceTicket(String ticket) {
            super(ticket);
        }

        @Override
        protected long getTimeOutSeconds() {
            return Long.parseLong(grantedTicketTimeOutSeconds);
        }
    }
}
