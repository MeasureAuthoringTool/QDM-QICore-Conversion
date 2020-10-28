package gov.cms.mat.fhir.services.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.SearchTotalModeEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import gov.cms.mat.fhir.services.config.security.SecurityFilter;
import gov.cms.mat.fhir.services.service.packaging.dto.PackageFormat;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class HapiFhirServer {
    private static final String CACHE_HEADER_NAME = "Cache-Control";
    private static final String CACHE_HEADER_VALUE = "no-cache";

    @Value("${mat-api-key}")
    private String matApiKey;

    @Getter
    private final FhirContext ctx;
    private final RestTemplate restTemplate;
    @Getter
    IGenericClient hapiClient;
    @Getter
    @Value("${fhir.r4.baseurl}")
    private String baseURL;

    public HapiFhirServer(FhirContext ctx, RestTemplate restTemplate) {
        this.ctx = ctx;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void setUp() {
        hapiClient = ctx.newRestfulGenericClient(baseURL);
        hapiClient.registerInterceptor(createLoggingInterceptor());

        log.info("Created hapi client for server: {} ", baseURL);
    }

    public Optional<ValueSet> fetchHapiValueSet(String oid) {
        return findResourceInBundle(getValueSetBundle(oid), ValueSet.class);
    }

    public Optional<Library> fetchHapiLibrary(String id) {
        return findResourceInBundle(getLibraryBundle(id), Library.class);
    }

    public Optional<Library> fetchHapiLibrary(String name, String version) {
        return findResourceInBundle(fetchLibraryBundleByVersionAndName(version, name), Library.class);
    }

    public Optional<Measure> fetchHapiMeasure(String id) {
        return findResourceInBundle(getMeasureBundle(id), Measure.class);
    }

    public <T extends Resource> Optional<T> findResourceInBundle(Bundle bundle, Class<T> clazz) {
        if (bundle.hasEntry()) {
            if (bundle.getEntry().size() > 1) {
                log.error("Hapi-Fhir Resource for {} returned more than one resource count: {}",
                        clazz.getSimpleName(), bundle.getEntry().size());
                return Optional.empty();
            } else {
                return findResourceFromBundle(bundle, clazz);
            }
        } else {
            log.debug("Hapi-Fhir Resource for {} NOT found in DB.",
                    clazz.getSimpleName());
            return Optional.empty();
        }
    }

    public <T extends Resource> Optional<T> findResourceFromBundle(Bundle bundle, Class<T> clazz) {
        Resource resource = bundle.getEntry().get(0).getResource();

        if (clazz.isInstance(resource)) {
            log.debug("Hapi-Fhir Resource for {} found in DB.",
                    clazz.getSimpleName());
            return Optional.of((T) resource);
        } else {
            log.error("Hapi-Fhir Resource is of wrong type expected: {} found in bundle: {}",
                    clazz.getSimpleName(),
                    resource.getClass().getSimpleName());
            return Optional.empty();
        }
    }


    public Optional<String> processBundleLink(Bundle bundle) {
        if (bundle.hasEntry()) {
            if (bundle.getEntry().isEmpty()) {
                return Optional.of(bundle.getLink().get(0).getUrl());
            } else {
                return Optional.of(bundle.getEntry().get(0).getFullUrl());
            }
        } else {
            return Optional.empty();
        }
    }

    public String persist(Resource resource) {
        log.debug("Persisting resource {} with id {}",
                resource.getResourceType() != null ? resource.getResourceType().name() : "null",
                resource.getId());

        HttpHeaders map = new HttpHeaders();
        map.put("Content-type", List.of("application/json"));
        HttpEntity<String> request = new HttpEntity<>(toJson(resource), map);
        String url = baseURL + "/" + resource.getResourceType().name() + "/" + resource.getId();
        restTemplate.put(url, request, new HashMap<>());
        return url;
    }

    public String buildResourceUrl(Resource resource) {
        return buildHapiFhirUrl(resource.fhirType(), resource.getId());
    }

    public String buildHapiFhirUrl(String type, String id) {
        return new StringBuilder(hapiClient.getServerBase())
                .append(type)
                .append('/')
                .append(id)
                .toString();
    }


    private LoggingInterceptor createLoggingInterceptor() {
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLogger(log);

        // Optionally you may configure the interceptor (by default only summary info is logged)
        loggingInterceptor.setLogRequestBody(false);
        loggingInterceptor.setLogRequestHeaders(false);
        loggingInterceptor.setLogRequestSummary(log.isDebugEnabled());

        loggingInterceptor.setLogResponseBody(false);
        loggingInterceptor.setLogResponseHeaders(false);
        loggingInterceptor.setLogResponseSummary(log.isDebugEnabled());

        return loggingInterceptor;
    }

    public IBaseOperationOutcome delete(IBaseResource resource) {
        return hapiClient.delete()
                .resource(resource)
                .prettyPrint()
                .encodedJson()
                .withAdditionalHeader(SecurityFilter.MAT_API_KEY, matApiKey )
                .execute();
    }


    public Bundle getValueSetBundle(String oid) {
        return getOidBundle(ValueSet.class, oid);
    }

    public Bundle getCodeSystemBundle(String oid) {
        return getOidBundle(CodeSystem.class, oid);
    }

    private Bundle getOidBundle(Class<? extends IBaseResource> resource, String oid) {
        return hapiClient.search()
                .forResource(resource)
                .where(ValueSet.IDENTIFIER.exactly().systemAndIdentifier("urn:ietf:rfc:3986", oid))
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .withAdditionalHeader(SecurityFilter.MAT_API_KEY, matApiKey )
                .execute();
    }

    public Bundle getMeasureBundle(String id) {
        return hapiClient.search()
                .forResource(Measure.class)
                .where(new TokenClientParam("_id").exactly().code(id))
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .withAdditionalHeader(SecurityFilter.MAT_API_KEY, matApiKey )
                .execute();
    }

    public Bundle getLibraryBundle(String id) {
        return hapiClient.search()
                .forResource(Library.class)
                .where(new TokenClientParam("_id").exactly().code(id))
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .withAdditionalHeader(SecurityFilter.MAT_API_KEY, matApiKey )
                .execute();
    }

    public Bundle fetchLibraryBundleByVersionAndName(String version, String name) {
        return hapiClient.search()
                .forResource(Library.class)
                .where(Library.VERSION.exactly().code(version))
                .and(Library.NAME.matches().value(name))
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .withAdditionalHeader(SecurityFilter.MAT_API_KEY, matApiKey )
                .execute();
    }

    public Bundle fetchLibraryBundleByName(String name) {
        return hapiClient.search()
                .forResource(Library.class)
                .and(Library.NAME.matches().value(name))
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .withAdditionalHeader(SecurityFilter.MAT_API_KEY, matApiKey )
                .execute();
    }

    public int count(Class<? extends Resource> resourceClass) {
        return hapiClient.search()
                .forResource(resourceClass)
                .totalMode(SearchTotalModeEnum.ACCURATE)
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .withAdditionalHeader(SecurityFilter.MAT_API_KEY, matApiKey )
                .execute()
                .getTotal();
    }

    public Bundle getAll(Class<? extends Resource> resourceClass) {
        return hapiClient.search()
                .forResource(resourceClass)
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .withAdditionalHeader(SecurityFilter.MAT_API_KEY, matApiKey )
                .execute();
    }

    public Bundle getNextPage(Bundle bundle) {
        return hapiClient.loadPage()
                .next(bundle)
                .withAdditionalHeader(SecurityFilter.MAT_API_KEY, matApiKey )
                .execute();
    }

    public String formatResource(Resource resource, PackageFormat packageFormat) {
        switch (packageFormat) {
            case XML:
                return toXml(resource);
            case JSON:
                return toJson(resource);
            default:
                throw new IllegalArgumentException("Format invalid " + packageFormat);
        }
    }

    public String toJson(Resource resource) {
        return ctx.newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(resource);
    }

    public String toXml(Resource resource) {
        return ctx.newXmlParser()
                .encodeResourceToString(resource);
    }

    public <T extends Resource> T parseResource(Class<T> resourceClass, String resourceJson) {
        return ctx.newJsonParser()
                .parseResource(resourceClass, resourceJson);
    }
}
