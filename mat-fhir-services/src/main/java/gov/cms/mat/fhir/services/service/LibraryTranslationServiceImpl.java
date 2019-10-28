package gov.cms.mat.fhir.services.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import gov.cms.mat.fhir.commons.model.Measure;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.objects.TranslationOutcome;
import gov.cms.mat.fhir.services.translate.LibraryMapper;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Library;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LibraryTranslationServiceImpl implements LibraryTranslationService {
    @Value("${fhir.r4.baseurl}")
    private String baseURL;



    @Override
    public TranslationOutcome translate(Measure qdmMeasure, MeasureExport measureExport) {
        TranslationOutcome res = new TranslationOutcome();

        try {
            Library library = new LibraryMapper(measureExport).translateToFhir();

//            Bundle bundle = new Bundle();
//            bundle.setType(Bundle.BundleType.TRANSACTION);
//            bundle.addEntry().setResource(library)
//                    .getRequest()
//                    .setUrl("Library")
//                    .setMethod(Bundle.HTTPVerb.POST);

            FhirContext ctx = FhirContext.forR4();
            IGenericClient client = ctx.newRestfulGenericClient(baseURL);

            MethodOutcome  methodOutcome = create(client, library);

           // Bundle resp = client.transaction().withBundle(bundle).execute();

           // org.hl7.fhir.r4.model.Library libraryResponse = (Library) resp.getEntry().get(0).getResource();

            res.setFhirIdentity(methodOutcome.getId().getIdPart());

          //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(resp));

        } catch (Exception ex) {
            res.setSuccessful(Boolean.FALSE);
            res.setMessage("/qdmtofhir/translateLibrary Failed " + qdmMeasure.getId() + " " + ex.getMessage());
            log.debug("Failed to Translate Measure id: {} ", qdmMeasure.getId(), ex);
        }
        return res;
    }

    public MethodOutcome create(IGenericClient client, IBaseResource resource) {
        return client.create()
                .resource(resource)
                .prettyPrint()
                .encodedJson()
                .execute();
    }

}
