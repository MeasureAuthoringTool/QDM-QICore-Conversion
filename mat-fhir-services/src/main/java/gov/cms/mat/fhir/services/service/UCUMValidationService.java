package gov.cms.mat.fhir.services.service;

import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class UCUMValidationService {
    @Value("classpath:ucum/ucum-essence.xml")
    private Resource resource;

    private UcumService ucumService;

    @PostConstruct
    void postConstruct() {
        try {
            ucumService = new UcumEssenceService(resource.getInputStream());
        } catch (UcumException | IOException e) {
            throw new InvalidPropertyException(this.getClass(), "resource", e.getMessage());
        }
    }

    public Boolean validate(String unit) {
        String result = ucumService.validate(unit);

        //Returns:null if valid
        return result == null;
    }
}
