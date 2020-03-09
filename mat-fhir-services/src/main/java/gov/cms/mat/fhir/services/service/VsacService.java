package gov.cms.mat.fhir.services.service;

import org.springframework.stereotype.Service;
import org.vsac.VSACResponseResult;

import gov.cms.mat.fhir.services.components.vsac.VsacClient;
import gov.cms.mat.fhir.services.components.vsac.VsacConverter;
import lombok.extern.slf4j.Slf4j;
import mat.model.VSACValueSetWrapper;

@Service
@Slf4j
public class VsacService {
    private final VsacClient vsacClient;
    private final VsacConverter vsacConverter;

    public VsacService(VsacClient vsacClient, VsacConverter vsacConverter) {
        this.vsacClient = vsacClient;
        this.vsacConverter = vsacConverter;
    }

    public VSACValueSetWrapper getData(String oid, String vsacGrantingTicket) {
        String ticket = getServiceTicket(vsacGrantingTicket);

        VSACResponseResult vsacResponseResult = vsacClient.getDataFromProfile(oid, ticket);

        if (isSuccessFull(vsacResponseResult)) {
            try {
                return processResponse(vsacResponseResult);
            } catch (Exception e) {
                log.warn("Cannot get XMl from vsac oid: {}", oid);
                return null;
            }
        } else {
            log.warn("Error response from the vsac service, result: {}", vsacResponseResult);
            return null;
        }
    }

    private VSACValueSetWrapper processResponse(VSACResponseResult vsacResponseResult) {
        return vsacConverter.toWrapper(vsacResponseResult.getXmlPayLoad());
    }

    private boolean isSuccessFull(VSACResponseResult vsacResponseResult) {
        return vsacResponseResult != null &&
                vsacResponseResult.getXmlPayLoad() != null &&
                !vsacResponseResult.isIsFailResponse();
    }


    private String getServiceTicket(String vsacGrantingTicket) {
        String serviceTicket = vsacClient.getServiceTicket(vsacGrantingTicket);
        log.debug("serviceTicket: {}", serviceTicket);
        return serviceTicket;
    }
    
    public String getGrantingTicket(String username, String passwd) {
        String grantingTicket = vsacClient.getGrantingTicket(username, passwd);
        log.debug("grantingTicket: {}", grantingTicket);
        return grantingTicket;
    }
}
