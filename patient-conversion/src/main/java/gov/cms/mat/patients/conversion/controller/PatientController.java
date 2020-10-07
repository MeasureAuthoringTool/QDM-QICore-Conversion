package gov.cms.mat.patients.conversion.controller;

import gov.cms.mat.patients.conversion.dao.BonniePatient;
import gov.cms.mat.patients.conversion.data.ConversionResult;
import gov.cms.mat.patients.conversion.service.PatientService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PutMapping("/convertOne")
    public ConversionResult convertOne(@RequestBody BonniePatient bonniePatient) {
        return patientService.processOne(bonniePatient);
    }

    @PutMapping("/convertMany")
    public List<ConversionResult> convertMany(@RequestBody List<BonniePatient> bonniePatients) {
        return patientService.processMany(bonniePatients);
    }
}
