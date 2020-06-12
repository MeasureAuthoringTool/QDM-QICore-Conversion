package gov.cms.mat.fhir.services.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.SearchTotalModeEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import gov.cms.mat.fhir.services.exceptions.HapiFhirCreateMeasureException;
import gov.cms.mat.fhir.services.service.packaging.dto.PackageFormat;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class HapiFhirServer {
    private static final String CACHE_HEADER_NAME = "Cache-Control";
    private static final String CACHE_HEADER_VALUE = "no-cache";
    @Getter
    FhirContext ctx;

    @Getter
    IGenericClient hapiClient;

    @Getter
    @Value("${fhir.r4.baseurl}")
    private String baseURL;

    @PostConstruct
    public void setUp() {
        ctx = FhirContext.forR4();

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

    public Optional<Library> fetchHapiLibrary(String name,String version) {
        return findResourceInBundle(fetchLibraryBundleByVersionAndName(version,name), Library.class);
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
        Bundle bundle = createAndExecuteBundle(resource);

        validatePersistedBundle(resource, bundle);

        //todo this is wrong without it we cannot find object after we create
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            log.debug("InterruptedException", e);
        }

        return buildResourceUrl(resource);
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

    private void validatePersistedBundle(Resource resource, Bundle bundle) {
        if (CollectionUtils.isEmpty(bundle.getEntry()) || bundle.getEntry().size() > 1) {
            log.error("Bundle size is invalid: {}", bundle.getEntry() != null ? bundle.getEntry().size() : null);
            throw new HapiFhirCreateMeasureException(resource.getIdElement().getValue());
        }

        Bundle.BundleEntryComponent bundleEntryComponent = bundle.getEntry().get(0);

        if (!bundleEntryComponent.hasResponse()) {
            log.error("Bundle does not contain a response");
            throw new HapiFhirCreateMeasureException(resource.getIdElement().getValue());
        }

        if (bundleEntryComponent.getResponse().getStatus() != null &&
                bundleEntryComponent.getResponse().getStatus().startsWith("20")) {
            log.debug("Successfully (OK) Persisted resource {} with id {}",
                    resource.getResourceType() != null ? resource.getResourceType().name() : "null",
                    resource.getId());
        } else {
            log.error("FAILED Persisted resource: {} with id: {} status:{}",
                    resource.getResourceType().name(), resource.getId(),
                    bundleEntryComponent.getResponse().getStatus());
            throw new HapiFhirCreateMeasureException(resource.getIdElement().getValue());
        }
    }

    public Bundle createAndExecuteBundle(Resource resource) {
        Bundle bundle = buildBundle(resource);

        return hapiClient.transaction()
                .withBundle(bundle)
                .execute();
    }

    Bundle buildBundle(Resource resource) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.TRANSACTION);
        bundle.addEntry().setResource(resource)
                .getRequest()
                .setUrl(baseURL + resource.getResourceType().name() + "/" + resource.getId())
                .setMethod(Bundle.HTTPVerb.PUT);
        return bundle;
    }

    private LoggingInterceptor createLoggingInterceptor() {
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        loggingInterceptor.setLogger(log);

        // Optionally you may configure the interceptor (by default only summary info is logged)
        loggingInterceptor.setLogRequestBody(false);
        loggingInterceptor.setLogRequestHeaders(false);
        loggingInterceptor.setLogRequestSummary(true);

        loggingInterceptor.setLogResponseBody(false);
        loggingInterceptor.setLogResponseHeaders(false);
        loggingInterceptor.setLogResponseSummary(true);

        return loggingInterceptor;
    }

    public IBaseOperationOutcome delete(IBaseResource resource) {
        return hapiClient.delete()
                .resource(resource)
                .prettyPrint()
                .encodedJson()
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
                .execute();
    }

    public Bundle getMeasureBundle(String id) {
        return hapiClient.search()
                .forResource(Measure.class)
                .where(new TokenClientParam("_id").exactly().code(id))
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .execute();
    }

    public Bundle getLibraryBundle(String id) {
        return hapiClient.search()
                .forResource(Library.class)
                .where(new TokenClientParam("_id").exactly().code(id))
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .execute();
    }

    public Bundle fetchLibraryBundleByVersionAndName(String version, String name) {
        return hapiClient.search()
                .forResource(Library.class)
                .where(Library.VERSION.exactly().code(version))
                .and(Library.NAME.matches().value(name))
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .execute();
    }

    public int count(Class<? extends Resource> resourceClass) {
        return hapiClient.search()
                .forResource(resourceClass)
                .totalMode(SearchTotalModeEnum.ACCURATE)
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .execute()
                .getTotal();
    }

    public Bundle getAll(Class<? extends Resource> resourceClass) {
        return hapiClient.search()
                .forResource(resourceClass)
                .returnBundle(Bundle.class)
                .withAdditionalHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE)
                .execute();
    }

    public Bundle getNextPage(Bundle bundle) {
        return hapiClient.loadPage()
                .next(bundle)
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
