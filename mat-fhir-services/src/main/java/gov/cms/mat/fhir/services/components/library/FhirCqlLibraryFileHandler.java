package gov.cms.mat.fhir.services.components.library;

import gov.cms.mat.cql.elements.LibraryProperties;
import gov.cms.mat.cql.parsers.ParseException;
import gov.cms.mat.fhir.rest.dto.ConversionType;
import gov.cms.mat.fhir.services.components.mongo.ConversionReporter;
import gov.cms.mat.fhir.services.components.mongo.ConversionResultsService;
import gov.cms.mat.fhir.services.components.xml.XmlSource;
import gov.cms.mat.fhir.services.config.LibraryConversionFileConfig;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import gov.cms.mat.fhir.services.service.CQLLibraryTranslationService;
import gov.cms.mat.fhir.services.translate.FhirLibraryTranslator;
import gov.cms.mat.fhir.services.translate.creators.FhirCreator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Library;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class FhirCqlLibraryFileHandler implements FileHandler, FhirCreator {
    private static final boolean SHOW_WARNINGS = false;

    private final HapiFhirServer hapiFhirServer;
    private final CQLLibraryTranslationService cqlLibraryTranslationService;
    private final LibraryConversionFileConfig libraryConversionFileConfig;
    private final ConversionResultsService conversionResultsService;


    public FhirCqlLibraryFileHandler(HapiFhirServer hapiFhirServer,
                                     CQLLibraryTranslationService cqlLibraryTranslationService,
                                     LibraryConversionFileConfig libraryConversionFileConfig,
                                     ConversionResultsService conversionResultsService) {
        this.hapiFhirServer = hapiFhirServer;
        this.cqlLibraryTranslationService = cqlLibraryTranslationService;
        this.libraryConversionFileConfig = libraryConversionFileConfig;
        this.conversionResultsService = conversionResultsService;
    }

    public void loaLibs() {
        libraryConversionFileConfig.getOrder().stream()
                .map(this::getData)
                .forEach(cql -> processHapiFhir(cql));
    }

    private String getData(String name) {
        log.debug("Processing include file: {} ", name);

        try (InputStream i = getClass().getResourceAsStream("/fhir/" + name)) {
            return new String(i.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void processHapiFhir(String cql) {

        String[] lines = cql.split("\\r?\\n");
        LibraryProperties libraryProperties = getLibrary(lines);

        try {
            String uuid = createLibraryUuid(libraryProperties);

            Optional<Library> library = hapiFhirServer.fetchHapiLibrary(uuid);

            AtomicBoolean atomicBoolean = new AtomicBoolean(true);

            if (library.isPresent()) {
                log.info("Already Exists Standard Fhir cql lib url: {}, : Properties: {}",
                        library.get().getUrl(), libraryProperties);
            } else {
                convert(cql, libraryProperties, uuid, atomicBoolean);
            }
        } catch (Exception e) {
            log.error("Error processing Standard Fhir cql lib url: {}", libraryProperties, e);
        }
    }

    private void convert(String cql, LibraryProperties libraryProperties, String uuid, AtomicBoolean atomicBoolean) {
        try {
            ConversionReporter.setInThreadLocal(uuid,
                    "Load-default-libs",
                    conversionResultsService,
                    Instant.now(),
                    ConversionType.CONVERSION,
                    XmlSource.SIMPLE,
                    SHOW_WARNINGS,
                    null);

            String elm = cqlLibraryTranslationService.convertToJsonFromFhirCql(atomicBoolean, cql, false);

            FhirLibraryTranslator fhirLibraryTranslator = new FhirLibraryTranslator(cql.getBytes(),
                    elm.getBytes(),
                    hapiFhirServer.getBaseURL());

            Library hapiFhirLibrary = fhirLibraryTranslator.translateToFhir(null);
            String url = hapiFhirServer.persist(hapiFhirLibrary);
            log.info("Created Standard Fhir cql lib url: {}, : Properties: {}", url, libraryProperties);

        } finally {
            ConversionReporter.removeInThreadLocalAndComplete();
        }
    }

    private LibraryProperties getLibrary(String[] lines) {
        return Arrays.stream(lines)
                .filter(l -> l.startsWith("library"))
                .map(this::buildLibraryProperties)
                .findFirst()
                .orElseThrow(() -> new ParseException("Cannot find library"));
    }

    private LibraryProperties buildLibraryProperties(String line) {
        return LibraryProperties.builder()
                .name(getLibraryName(line))
                .version(getLibraryVersion(line))
                .line(line)
                .build();
    }

    private String getLibraryName(String line) {
        return StringUtils.substringBetween(line, "library ", " version ");
    }

    private String getLibraryVersion(String line) {
        return StringUtils.substringBetween(line, " version '", "'");
    }
}
