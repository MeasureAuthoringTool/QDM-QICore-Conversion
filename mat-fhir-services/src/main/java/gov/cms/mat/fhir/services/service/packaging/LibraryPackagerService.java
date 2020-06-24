package gov.cms.mat.fhir.services.service.packaging;

import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.cql.CQLAntlrUtils;
import gov.cms.mat.fhir.services.exceptions.FhirIncludeLibrariesNotFoundException;
import gov.cms.mat.fhir.services.exceptions.FhirNotUniqueException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceValidationException;
import gov.cms.mat.fhir.services.exceptions.cql.LibraryAttachmentNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorProcessor;
import gov.cms.mat.fhir.services.rest.support.FhirValidatorService;
import gov.cms.mat.fhir.services.service.packaging.dto.LibraryPackageFullHapi;
import gov.cms.mat.fhir.services.translate.creators.FhirLibraryHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.gen.cqlBaseVisitor;
import org.cqframework.cql.gen.cqlParser;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.DataRequirement;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LibraryPackagerService implements FhirValidatorProcessor, FhirLibraryHelper {
    private final HapiFhirServer hapiFhirServer;
    private final FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;
    private final FhirValidatorService fhirValidatorService;
    private final CQLAntlrUtils cqlAntlrUtils;

    public LibraryPackagerService(HapiFhirServer hapiFhirServer,
                                  FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor,
                                  FhirValidatorService fhirValidatorService,
                                  CQLAntlrUtils cqlAntlrUtils) {
        this.hapiFhirServer = hapiFhirServer;
        this.fhirIncludeLibraryProcessor = fhirIncludeLibraryProcessor;
        this.fhirValidatorService = fhirValidatorService;
        this.cqlAntlrUtils = cqlAntlrUtils;
    }

    public Library packageMinimum(String id) {
        Library library = fetchLibraryFromHapi(id);

        FhirResourceValidationResult result = fhirValidatorService.validate(library);

        if (CollectionUtils.isNotEmpty(result.getValidationErrorList())) {
            boolean hasErrors = result.getValidationErrorList().stream().
                    anyMatch(ve -> !StringUtils.equals("WARNING",ve.getSeverity()) &&
                            !StringUtils.equals("INFORMATION",ve.getSeverity()));
            if (hasErrors) {
                log.error("Validation errors encountered: " , result.getValidationErrorList());
                throw new HapiResourceValidationException(id, "Library");
            }
        }
        return library;
    }

    public Library fetchLibraryFromHapi(String id) {
        Bundle bundle = hapiFhirServer.getLibraryBundle(id);

        if (bundle.hasEntry()) {
            if (bundle.getEntry().size() > 1) {
                throw new FhirNotUniqueException("Library id:" + id, bundle.getEntry().size());
            } else {
                return (Library) bundle.getEntry().get(0).getResource();
            }
        } else {
            throw new HapiResourceNotFoundException(id, "Library");
        }
    }

    private String createLibraryName(Library library) {
        return library.getName() + "-" + library.getVersion();
    }

    public LibraryPackageFullHapi packageFull(String id) {
        Library library = packageMinimum(id);

        Bundle includedLibraryBundle = buildIncludeBundle(library, id);

        return LibraryPackageFullHapi.builder()
                .library(library)
                .includeBundle(includedLibraryBundle)
                .build();
    }

    Bundle buildIncludeBundle(Library measureLib, String id) {
        Bundle includedLibraryBundle = new Bundle();
        // Using Bryns connecthathon examples use Transactions so they work with hapi-fhir.
        includedLibraryBundle.setType(Bundle.BundleType.TRANSACTION);

        includedLibraryBundle.addEntry().setResource(measureLib).getRequest()
                        .setUrl("Library/" + getFhirId(measureLib))
                        .setMethod(Bundle.HTTPVerb.PUT);

        FhirIncludeLibraryResult result = findIncludedFhirLibraries(measureLib);
        processIncludedLibraries(includedLibraryBundle, result, id);

        var visitor = new ValueSetCodeSystemVisitor();
        var libContext = cqlAntlrUtils.getLibraryContextBytes(Base64.getDecoder().decode(measureLib.getContent().get(0).getData()));
        visitor.visit(libContext);
        result.getLibraryReferences().stream().forEach(l -> {
            var c = cqlAntlrUtils.getLibraryContextBytes(Base64.getDecoder().decode(measureLib.getContent().get(0).getData()));
            visitor.visit(c);
        });

        //This is wrong but we don't have direction on how to do this correctly yet.
        visitor.getCodeSystems().forEach(cs -> {
            includedLibraryBundle.addEntry().setResource(cs);
        });
        //This is wrong but we don't have direction on how to do this correctly yet.
        visitor.getValueSets().forEach(vs -> {
            includedLibraryBundle.addEntry().setResource(vs);
        });

        //Include code systems (This will be done once we figure out which url to use.)
        return includedLibraryBundle;
    }

    private FhirIncludeLibraryResult findIncludedFhirLibraries(Library library) {
        if (CollectionUtils.isEmpty(library.getContent())) {
            throw new LibraryAttachmentNotFoundException(library);
        } else {
            return findIncludedLibraries(library, fhirIncludeLibraryProcessor);
        }
    }


    private void processIncludedLibraries(Bundle libraryBundle, FhirIncludeLibraryResult fhirIncludeLibraryResult, String id) {
        List<FhirIncludeLibraryReferences> libraryReferences = fhirIncludeLibraryResult.getLibraryReferences();

        List<String> missingLibs = libraryReferences.stream()
                .filter(r -> r.getLibrary() == null)
                .map(r -> r.getName() + '-' + r.getVersion())
                .collect(Collectors.toList());

        if (missingLibs.isEmpty()) {
            for (FhirIncludeLibraryReferences reference : libraryReferences) {
                Library library = reference.getLibrary();
                library.setId(createLibraryName(library));
                libraryBundle.addEntry().setResource(library)
                        .getRequest()
                        .setUrl("Library/" + getFhirId(library))
                        .setMethod(Bundle.HTTPVerb.PUT);;
            }
        } else {
            String missing = String.join(", ", missingLibs);
            throw new FhirIncludeLibrariesNotFoundException(id, missing);
        }
    }

    private String getFhirId(Resource r) {
        String id = r.getId();
        int endIndex = id.indexOf("/_history");
        if (endIndex >= 0) {
            id = id.substring(0, endIndex);
        }
        int startIndex = id.lastIndexOf('/') + 1;
        return id.substring(startIndex);
    }

    @Getter
    @Slf4j
    private static class ValueSetCodeSystemVisitor extends cqlBaseVisitor<String> {
        List<ValueSet> valueSets = new ArrayList<>();
        List<CodeSystem> codeSystems = new ArrayList<>();;

        private ValueSetCodeSystemVisitor() {
        }

        /**
         * Stores off valueSets ,they are used later when looking up retrieves.
         *
         * @param ctx The context.
         * @return Always null.
         */
        @Override
        public String visitValuesetDefinition(cqlParser.ValuesetDefinitionContext ctx) {
            String url = ctx.valuesetId().getText();
            var existingVs = valueSets.stream().filter(vs -> StringUtils.equals(vs.getUrl(),url)).findFirst();
            if (existingVs.isEmpty()) {
                ValueSet valueSet = new ValueSet();
                valueSet.setUrl(url);
                valueSets.add(valueSet);
            }
            return null;
        }

        public String visitCodesystemDefinition(cqlParser.CodesystemDefinitionContext ctx) {
            String url = ctx.codesystemId().getText();
            var existingCs = codeSystems.stream().filter(cs -> StringUtils.equals(cs.getUrl(),url)).findFirst();
            if (existingCs.isEmpty()) {
                CodeSystem codeSystem = new CodeSystem();
                codeSystem.setUrl(url);
                codeSystems.add(codeSystem);
            }
            return null;
        }
    }
}
