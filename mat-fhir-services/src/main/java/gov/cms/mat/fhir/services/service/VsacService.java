package gov.cms.mat.fhir.services.service;

import gov.cms.mat.fhir.services.components.vsac.ValueSetVSACResponseResult;
import gov.cms.mat.fhir.services.components.vsac.VsacConverter;
import gov.cms.mat.fhir.services.components.vsac.VsacRestClient;
import lombok.extern.slf4j.Slf4j;
import mat.model.VSACValueSetWrapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VsacService {
    private final VsacRestClient vsacRestClient;
    private final VsacConverter vsacConverter;

    public VsacService(VsacRestClient vsacRestClient, VsacConverter vsacConverter) {
        this.vsacRestClient = vsacRestClient;
        this.vsacConverter = vsacConverter;
    }

    public ValueSetVSACResponseResult getValueSetVSACResponseResult(String oid, String vsacGrantingTicket) {
        String ticket = getServiceTicket(vsacGrantingTicket);

        return vsacRestClient.getDataFromProfile(oid, ticket);
    }

    public VSACValueSetWrapper getVSACValueSetWrapper(String oid, String vsacGrantingTicket) {
        ValueSetVSACResponseResult vsacResponseResult = getValueSetVSACResponseResult(oid, vsacGrantingTicket);

        if (isSuccessFull(vsacResponseResult)) {
            try {
                return vsacConverter.toWrapper(vsacResponseResult.getXmlPayLoad());
            } catch (Exception e) {
                log.warn("Cannot get XMl from vsac oid: {}, reason: {}", oid, vsacResponseResult.getFailReason());
                return null;
            }
        } else {
            log.warn("Error response from the vsac service, result: {}", vsacResponseResult);
            return null;
        }
    }


    private boolean isSuccessFull(ValueSetVSACResponseResult vsacResponseResult) {
        return vsacResponseResult != null &&
                vsacResponseResult.getXmlPayLoad() != null &&
                !vsacResponseResult.isFailResponse();
    }


    public String getServiceTicket(String vsacGrantingTicket) {
        String serviceTicket = vsacRestClient.fetchSingleUseTicket(vsacGrantingTicket);
        log.trace("serviceTicket: {}", serviceTicket);
        return serviceTicket;
    }

    public String getGrantingTicket(String username, String passwd) {
        String grantingTicket = vsacRestClient.fetchGrantingTicket(username, passwd);
        log.trace("grantingTicket: {}", grantingTicket);
        return grantingTicket;
    }
}
