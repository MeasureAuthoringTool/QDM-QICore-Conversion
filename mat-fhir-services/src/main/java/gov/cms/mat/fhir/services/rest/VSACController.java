package gov.cms.mat.fhir.services.rest;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cms.mat.fhir.commons.model.CqlLibrary;
import gov.cms.mat.fhir.services.cql.LibraryCqlVisitor;
import gov.cms.mat.fhir.services.cql.LibraryCqlVisitorFactory;
import gov.cms.mat.fhir.services.cql.parser.CodeListService;
import gov.cms.mat.fhir.services.cql.parser.CqlUtils;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.repository.CqlLibraryRepository;
import gov.cms.mat.fhir.services.rest.support.ApiKeyResponseHeader;
import gov.cms.mat.fhir.services.translate.ValueSetMapper;
import gov.cms.mat.vsac.VsacService;
import gov.cms.mat.vsac.model.VsacCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/vsac")
@Tag(name = "VSAC-Controller",
        description = "API for acquiring Tickets from VSAC Service for Testing and Integration Purposes.")
@Slf4j
public class VSACController implements ApiKeyResponseHeader {
    private final VsacService vsacService;
    private final ValueSetMapper valueSetMapper;
    private final FhirContext fhirContext;
    private final CqlLibraryRepository cqlLibRepo;
    private final HapiFhirServer hapiFhirServer;
    private final LibraryCqlVisitorFactory libVisitorFactory;
    private final CodeListService codeListService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VSACController(VsacService vsacService, ValueSetMapper valueSetMapper, FhirContext fhirContext, CqlLibraryRepository cqlLibRepo, HapiFhirServer hapiFhirServer, LibraryCqlVisitorFactory libVisitorFactory, CodeListService codeListService) {
        this.vsacService = vsacService;
        this.valueSetMapper = valueSetMapper;
        this.fhirContext = fhirContext;
        this.cqlLibRepo = cqlLibRepo;
        this.hapiFhirServer = hapiFhirServer;
        this.libVisitorFactory = libVisitorFactory;
        this.codeListService = codeListService;
    }

    @Operation(summary = "Returns fhir resource bundle for the specified measure id.")
    @GetMapping(path = "/getFhirResources")
    public @ResponseBody
    JsonNode getFhirResources(@RequestParam String ulmsApiKey,
                              @RequestParam String measureId,
                              HttpServletResponse response) {

        try {
            Bundle result = new Bundle();
            result.setType(Bundle.BundleType.TRANSACTION);
            CqlLibrary cqlLib = cqlLibRepo.getCqlLibraryByMeasureId(measureId);
            Optional<Library> fhirLib = hapiFhirServer.fetchHapiLibrary(cqlLib.getCqlName(), cqlLib.getMatVersionFormat());

            fhirLib.ifPresent(l -> {
                String cql = new String(l.getContent().stream().filter(
                        c -> StringUtils.equals("text/cql", c.getContentType())).findAny().get().getData());
                LibraryCqlVisitor libVisitor = libVisitorFactory.visit(cql);

                libVisitor.getValueSets().forEach(mvs -> {
                    String oid = CqlUtils.parseOid(getOidFromValueSetUrl(getText(mvs.valuesetId())));
                    ValueSet fhirValueSet = valueSetMapper.mapToFhir(oid, null, ulmsApiKey);
                    result.addEntry().setResource(fhirValueSet).getRequest().setUrl("ValueSet/" +
                            fhirValueSet.getId()).setMethod(Bundle.HTTPVerb.PUT);
                });

                libVisitor.getCodeSystems().forEach(mcs -> {
                    String uri = getText(mcs.codesystemId());
                    String version = getText(mcs.versionSpecifier());
                    String oid = getOidFromUrn(codeListService.getOidToVsacCodeSystemMap().values().stream().filter(
                            cse -> StringUtils.equals(uri, cse.getUrl())).findFirst().get().getOid());

                    CodeSystem codeSystem = new CodeSystem();
                    codeSystem.setId(randomUUID());
                    codeSystem.setName(uri);
                    codeSystem.setUrl(uri);
                    codeSystem.setVersion(version);
                    libVisitor.getCodes().stream().filter(cdc -> StringUtils.equals(getText(mcs.identifier()), getText(cdc.codesystemIdentifier()))).forEach(cdc -> {
                        //CODE:/CodeSystem/SNOMEDCT/Version/2020-09/Code/29857009/Info
                        String path = "/CodeSystem/" + trimVersionFromCodeSystemIdentifier(getText(mcs.identifier())) + "/Version/" +
                                CqlUtils.parseMatVersionFromCodeSystemUri(version) +
                                "/Code/" + getText(cdc.codeId()) + "/Info";
                        VsacCode code = vsacService.getCode(path, ulmsApiKey);
                        VsacCode.VsacDataResultSet codeResult = code.getData().getResultSet().get(0);
                        codeSystem.addConcept().setCode(codeResult.getCode()).setDisplay(codeResult.getCodeName()).setDefinition(codeResult.getCodeName());
                    });
                    result.addEntry().setResource(codeSystem).getRequest().setUrl("CodeSystem/" + codeSystem.getId()).
                            setMethod(Bundle.HTTPVerb.PUT);
                });
            });

            return objectMapper.readTree(fhirContext.newJsonParser().encodeResourceToString(result));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            processResponseHeader(response);
        }
    }

    @Operation(summary = "Returns fhir for the specified vsac oid. Version is currently not supported but will be eventually.")
    @GetMapping(path = "/getFhirValueset")
    public @ResponseBody
    JsonNode getFhirValueset(@RequestParam String ticketGrantingTicket,
                             @RequestParam String oid,
                             @RequestParam(defaultValue = "") String version,
                             @RequestParam String apiKey,
                             HttpServletResponse response) {
        try {
        	return objectMapper.readTree(fhirContext.newJsonParser().encodeResourceToString(valueSetMapper.mapToFhir(oid, version, apiKey)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            processResponseHeader(response);
        }
    }

    private String randomUUID() {
        return StringUtils.upperCase(StringUtils.remove(UUID.randomUUID().toString(), '-'));
    }

    private String trimVersionFromCodeSystemIdentifier(String codeSystemIdentifier) {
        int lastColon = codeSystemIdentifier.lastIndexOf(":");
        if (lastColon > -1) {
            return codeSystemIdentifier.substring(0, lastColon);
        }
        return codeSystemIdentifier;
    }

    private String getOidFromUrn(String urn) {
        int lastColon = urn.lastIndexOf(":");
        if (lastColon > -1) {
            return urn.substring(lastColon + 1, urn.length());
        }
        return null;
    }

    private String getOidFromValueSetUrl(String url) {
        int lastSlash = url.lastIndexOf("/");
        if (lastSlash > -1) {
            return url.substring(lastSlash + 1, url.length()); //remove ' as well.
        }
        return null;
    }

    private String getText(ParserRuleContext c) {
        return c == null ? null : removeTics(removeQuotes(c.getText()));
    }

    private String removeTics(String s) {
        return StringUtils.removeEnd(StringUtils.removeStart(s, "'"), "'");
    }

    private String removeQuotes(String s) {
        return StringUtils.removeEnd(StringUtils.removeStart(s, "\""), "\"");
    }
}
