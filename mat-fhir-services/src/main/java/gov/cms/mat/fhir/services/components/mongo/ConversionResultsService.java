package gov.cms.mat.fhir.services.components.mongo;

import gov.cms.mat.fhir.rest.dto.*;
import gov.cms.mat.fhir.services.exceptions.LibraryConversionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ConversionResultsService {
    private final ConversionResultRepository conversionResultRepository;

    public ConversionResultsService(ConversionResultRepository conversionResultRepository) {
        this.conversionResultRepository = conversionResultRepository;
    }

    ConversionResult addValueSetResult(String measureId, String oid, String reason, boolean success, String link) {
        ConversionResult conversionResult = findOrCreate(measureId);

        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);
        valueSetConversionResults.setLink(null);
        valueSetConversionResults.setSuccess(success);
        valueSetConversionResults.setReason(reason);
        valueSetConversionResults.setLink(link);


        return conversionResultRepository.save(conversionResult);
    }

    ConversionResult addMeasureResult(String measureId, FieldConversionResult result) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getMeasureConversionResults() == null) {
            conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        }
        conversionResult.getMeasureConversionResults().getMeasureResults().add(result);
        return conversionResultRepository.save(conversionResult);
    }

    ConversionResult addLibraryResult(String measureId, FieldConversionResult result, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        LibraryConversionResults libraryConversionResult = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResult.getLibraryResults().add(result);

        return conversionResultRepository.save(conversionResult);
    }

    public Optional<ConversionResult> findByMeasureId(String measureId) {
        return conversionResultRepository.findByMeasureId(measureId);
    }

    public List<ConversionResult> findAll() {
        return conversionResultRepository.findAll(Sort.by(Sort.Direction.DESC, "modified"));
    }

    private synchronized ConversionResult findOrCreate(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            return optional.get();
        } else {
            ConversionResult conversionResult = new ConversionResult();
            conversionResult.setMeasureId(measureId);
            return conversionResult;
        }
    }

    void clearValueSetResults(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getValueSetConversionResults().clear();
            conversionResultRepository.save(conversionResult);
        } else {
            log.trace("ConversionResult not found for measureId: {}", measureId);
        }
    }

    synchronized void clearMeasure(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();

            if (conversionResult.getMeasureConversionResults() != null) {
                conversionResult.getMeasureConversionResults().getMeasureResults().clear();
                conversionResult.getMeasureConversionResults().setMeasureConversionType(null);
                conversionResult.getMeasureConversionResults().getMeasureFhirValidationResults().clear();
                conversionResultRepository.save(conversionResult);
            }
        } else {
            log.trace("ConversionResult not found for measureId: {}", measureId);
        }
    }

    synchronized void clearMeasureOrchestration(String measureId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        conversionResult.getValueSetConversionResults().clear();

        conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        conversionResult.getLibraryConversionResults().clear();
        conversionResult.setMeasureId(measureId);

        conversionResult.setErrorReason(null);

        conversionResultRepository.save(conversionResult);
    }

    public ConversionResult findConversionResult(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        return optional
                .orElseThrow(() ->
                        new LibraryConversionException("Cannot find ConversionResult for measureId: " + measureId));
    }

    public synchronized void setLibraryConversionType(String measureId,
                                                      ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.setConversionType(conversionType);

        conversionResultRepository.save(conversionResult);
    }


    synchronized void clearLibrary(String measureId) {
        Optional<ConversionResult> optional = findByMeasureId(measureId);

        if (optional.isPresent()) {
            ConversionResult conversionResult = optional.get();
            conversionResult.getLibraryConversionResults().clear();

            conversionResultRepository.save(conversionResult);
        } else {
            log.trace("Not found by measureId: {}", measureId);
        }
    }


    public synchronized void addCqlConversionResultSuccess(String measureId, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.TRUE);


        conversionResultRepository.save(conversionResult);
    }


    public synchronized void addCqlConversionErrorMessage(String measureId, String error, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);

        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getErrors().add(error);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addCql(String measureId, String cql, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.getCqlConversionResult().setCql(cql);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addElm(String measureId, String json, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.getCqlConversionResult().setElm(json);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addCqlConversionErrors(String measureId, List<CqlConversionError> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getCqlConversionErrors().addAll(errors);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addMatCqlConversionErrors(String measureId, List<MatCqlConversionException> errors, String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);
        libraryConversionResults.getCqlConversionResult().setResult(Boolean.FALSE);
        libraryConversionResults.getCqlConversionResult().getMatCqlConversionErrors().addAll(errors);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void setCqlConversionResult(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreate(measureId);


        //todo remove

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void setMeasureConversionType(String measureId, ConversionType conversionType) {
        ConversionResult conversionResult = findOrCreate(measureId);


        //todo remove

        conversionResult.setMeasureId(measureId);

        if (conversionResult.getMeasureConversionResults() == null) {
            conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        }
        conversionResult.getMeasureConversionResults().setMeasureConversionType(conversionType);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addFhirMeasureValidationResults(String measureId,
                                                             List<FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(measureId);

        if (conversionResult.getMeasureConversionResults() == null) {
            conversionResult.setMeasureConversionResults(new MeasureConversionResults());
        }

        conversionResult.getMeasureConversionResults().setMeasureFhirValidationResults(list);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addLibraryValidationResults(String measureId,
                                                         List<FhirValidationResult> list,
                                                         String matLibraryId) {

        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.getLibraryFhirValidationResults().addAll(list);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addValueSetValidationResults(String measureId, String oid,
                                                          List<FhirValidationResult> list) {
        ConversionResult conversionResult = findOrCreate(measureId);
        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);
        valueSetConversionResults.getValueSetFhirValidationResults().addAll(list);

        conversionResultRepository.save(conversionResult);
    }


    public synchronized void addValueSetValidationResult(String measureId,
                                                         String oid,
                                                         FhirValidationResult fhirValidationResult) {
        ConversionResult conversionResult = findOrCreate(measureId);
        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);
        valueSetConversionResults.getValueSetFhirValidationResults().add(fhirValidationResult);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addValueSetValidationError(String measureId,
                                                        String oid,
                                                        String error) {
        ConversionResult conversionResult = findOrCreate(measureId);
        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);

        valueSetConversionResults.setSuccess(Boolean.FALSE);
        valueSetConversionResults.setReason(error);
        valueSetConversionResults.setLink(null);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addValueSetValidationLink(String measureId,
                                                       String oid,
                                                       String link,
                                                       String message) {
        ConversionResult conversionResult = findOrCreate(measureId);
        ValueSetConversionResults valueSetConversionResults = conversionResult.findOrCreateValueSetConversionResults(oid);

        valueSetConversionResults.setSuccess(Boolean.TRUE);
        valueSetConversionResults.setReason(message);
        valueSetConversionResults.setLink(link);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addLibraryValidationLink(String measureId,
                                                      String link,
                                                      String message,
                                                      String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.setLink(link);
        libraryConversionResults.setReason(message);
        libraryConversionResults.setSuccess(Boolean.TRUE);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addLibraryValidationError(String measureId,
                                                       String message,
                                                       String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.setLink(null);
        libraryConversionResults.setReason(message);
        libraryConversionResults.setSuccess(Boolean.FALSE);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void addLibraryNotFoundInHapi(String measureId,
                                                      String matLibraryId) {
        ConversionResult conversionResult = findOrCreate(measureId);
        LibraryConversionResults libraryConversionResults = conversionResult.findOrCreateLibraryConversionResults(matLibraryId);

        libraryConversionResults.setLink(null);
        libraryConversionResults.setReason("Not found in Hapi");
        libraryConversionResults.setSuccess(Boolean.FALSE);

        conversionResultRepository.save(conversionResult);
    }

    public synchronized void save(ConversionResult conversionResult) {
        conversionResultRepository.save(conversionResult);
    }

    public void addErrorMessage(String measureId, String message) {
        ConversionResult conversionResult = findOrCreate(measureId);
        conversionResult.setErrorReason(message);
        conversionResultRepository.save(conversionResult);
    }

//    public ValueSetResult findValueSetValueSetResult(String oid, ConversionResult conversionResult) {
//        return conversionResult.getValueSetConversionResults()
//                .stream()
//                .filter(v -> v.getOid().equals(oid))
//                .findFirst()
//                .orElseGet(() -> createValueSetValueSetResult(oid, conversionResult));
//    }

//    public ValueSetResult createValueSetValueSetResult(String oid, ConversionResult conversionResult) {
//        ValueSetResult valueSetResult = ValueSetResult.builder().oid(oid).build();
//
//        conversionResult.getValueSetConversionResults().getValueSetResults().add(valueSetResult);
//
//        return valueSetResult;
//    }


//    public ValueSetValidationResult findValueSetValidationResult(String oid, ConversionResult conversionResult) {
//
//        return conversionResult.getValueSetConversionResults()
//                .getValueSetFhirValidationResults()
//                .stream()
//                .filter(v -> v.getOid().equals(oid))
//                .findFirst()
//                .orElseGet(() -> createValueSetValidationResult(oid, conversionResult));
//    }

//    public ValueSetValidationResult createValueSetValidationResult(String oid, ConversionResult conversionResult) {
//        ValueSetValidationResult validationResult = new ValueSetValidationResult(oid);
//
//        conversionResult.getValueSetConversionResults().getValueSetFhirValidationResults().add(validationResult);
//
//        return validationResult;
//    }

//    public ConversionResult getConversionResultWithValueSetConversionResults(String measureId) {
//        ConversionResult conversionResult = findOrCreate(measureId);
//
//        if (conversionResult.getValueSetConversionResults() == null) {
//            conversionResult.setValueSetConversionResults(new ValueSetConversionResults());
//        }
//        return conversionResult;
//    }


}
