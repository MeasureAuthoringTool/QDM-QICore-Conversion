/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.services.service.orchestration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import gov.cms.mat.fhir.commons.objects.FhirResourceValidationResult;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryReferences;
import gov.cms.mat.fhir.rest.dto.FhirIncludeLibraryResult;
import gov.cms.mat.fhir.rest.packaging.FhirPackage;
import gov.cms.mat.fhir.rest.packaging.FhirPackagingOutcome;
import gov.cms.mat.fhir.services.components.fhir.FhirIncludeLibraryProcessor;
import gov.cms.mat.fhir.services.exceptions.CqlLibraryNotFoundException;
import gov.cms.mat.fhir.services.exceptions.HapiResourceNotFoundException;
import gov.cms.mat.fhir.services.hapi.HapiFhirLinkProcessor;
import gov.cms.mat.fhir.services.hapi.HapiFhirServer;
import static gov.cms.mat.fhir.services.translate.LibraryTranslatorBase.CQL_CONTENT_TYPE;
import static gov.cms.mat.fhir.services.translate.LibraryTranslatorBase.ELM_CONTENT_TYPE;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.springframework.stereotype.Service;

/**
 *
 * @author duanedecouteau
 */
@Service
@Slf4j
public class PackagingOrchestrationService {
    private String PATH = "/opt/fhir/distribution/";
    private String directoryName;
    private final HapiFhirServer hapiFhirServer;
    private final HapiFhirLinkProcessor hapiFhirLinkProcessor;
    private final FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor;
    private String primaryLibraryName;
    private String primaryLibraryVersion;
    private Bundle measureBundle;
    private Bundle libraryBundle;
    private FhirPackage fhirPackage;
    private byte[] zipPackage = null;
    private String humanReadible;
    private FhirPackagingOutcome packagingOutcome;
    private String format;
    private String id;
    private FhirIncludeLibraryResult fhirIncludeLibraryResult;
    

    
    public PackagingOrchestrationService(HapiFhirServer hapiFhirServer, HapiFhirLinkProcessor hapiFhirLinkProcessor, FhirIncludeLibraryProcessor fhirIncludeLibraryProcessor) {
        this.hapiFhirLinkProcessor = hapiFhirLinkProcessor;
        this.hapiFhirServer = hapiFhirServer;
        this.fhirIncludeLibraryProcessor = fhirIncludeLibraryProcessor;
    }
    
    
    
    public FhirPackage packageMinumum(String id, String format) {
        this.format = format;
        this.id = id;

        fhirPackage = new FhirPackage();
        measureBundle = new Bundle();
        libraryBundle = new Bundle();
        packagingOutcome = new FhirPackagingOutcome();
        packagingOutcome.setPackagingType("packageMinimum");
        packagingOutcome.setBundleFormat(format);
        
        Measure measure = getMeasureResource(id);
        
        
        Optional<Library> optionalLibrary = null;
        for (CanonicalType library : measure.getLibrary()) {
            optionalLibrary = hapiFhirLinkProcessor.fetchLibraryByUrl(library.getValueAsString());
        }
        
        if (optionalLibrary.isPresent()) {
            Library library = optionalLibrary.get();
            primaryLibraryName = library.getName();
            primaryLibraryVersion = library.getVersion();
            library.setId(primaryLibraryName+"-"+primaryLibraryVersion);            
            libraryBundle.setType(Bundle.BundleType.COLLECTION);
            libraryBundle.addEntry().setResource(library);
            // write out library components have to know what primary lib name and version are
            createDirectory();
            writeLibraryCQLandELM(library);
        }       

        boolean libraryVal = validateResource(libraryBundle, hapiFhirServer.getCtx(), "library");
        
        if (libraryVal) {
            fhirPackage.setLibraryPackage(getBundlePreferredFormat(libraryBundle));
        }
        else {
            //faile
            packagingOutcome.setPackagingMessage("Failed library Bundle Validation");
            fhirPackage.setPackagingOutcome(packagingOutcome);
            return fhirPackage;
        }
        
        measure.setId(primaryLibraryName+"-"+primaryLibraryVersion);
        measureBundle.setType(Bundle.BundleType.COLLECTION);
        BundleEntryComponent be = new BundleEntryComponent();
        measureBundle.addEntry().setResource(measure);
        
        boolean measureVal = validateResource(measureBundle, hapiFhirServer.getCtx(), "measure");
        
        if (measureVal) {
            fhirPackage.setMeasurePackage(getBundlePreferredFormat(measureBundle));
        }
        else {
            //fail
            packagingOutcome.setPackagingMessage("Failed Measure Bundle Validation");
            fhirPackage.setPackagingOutcome(packagingOutcome);
            return fhirPackage;
        }
        
        //write results to disk for later
        writeLibraryBundle(fhirPackage.getLibraryPackage());
        writeMeasureBundle(fhirPackage.getMeasurePackage());
        
        packagingOutcome.setName(primaryLibraryName);
        packagingOutcome.setVersion(primaryLibraryVersion);
        packagingOutcome.setResult(true);
        fhirPackage.setPackagingOutcome(packagingOutcome);
        return fhirPackage;
    }
    
    public FhirPackage packageForBonnie(String id, String format) {
        this.format = format;
        this.id = id;
        fhirPackage = new FhirPackage();
        measureBundle = new Bundle();
        libraryBundle = new Bundle();
        packagingOutcome = new FhirPackagingOutcome();
        packagingOutcome.setPackagingType("packageForBonnie");
        packagingOutcome.setBundleFormat(format);
        
        Measure measure = getMeasureResource(id);
                
        Optional<Library> optionalLibrary = null;
        for (CanonicalType library : measure.getLibrary()) {
            optionalLibrary = hapiFhirLinkProcessor.fetchLibraryByUrl(library.getValueAsString());
        }
        
        if (optionalLibrary.isPresent()) {
            Library library = optionalLibrary.get();
            primaryLibraryName = library.getName();
            primaryLibraryVersion = library.getVersion();
            library.setId(primaryLibraryName+"-"+primaryLibraryVersion);
            libraryBundle.setType(Bundle.BundleType.COLLECTION);
            libraryBundle.addEntry().setResource(library);
            createDirectory();
            writeLibraryCQLandELM(library);
            fhirIncludeLibraryResult = findIncludedFhirLibraries(library);
        }
        
        processIncludedLibraries();
        
        boolean libraryVal = validateResource(libraryBundle, hapiFhirServer.getCtx(), "library");
        
        if (libraryVal) {
            fhirPackage.setLibraryPackage(getBundlePreferredFormat(libraryBundle));
        }
        else {
            //faile
            packagingOutcome.setPackagingMessage("Failed library Bundle Validation");
            fhirPackage.setPackagingOutcome(packagingOutcome);
            return fhirPackage;
        }
        
        measure.setId(primaryLibraryName+"-"+primaryLibraryVersion);
        measureBundle.setType(Bundle.BundleType.COLLECTION);
        BundleEntryComponent be = new BundleEntryComponent();
        measureBundle.addEntry().setResource(measure);
        
        boolean measureVal = validateResource(measureBundle, hapiFhirServer.getCtx(), "measure");
        
        if (measureVal) {
            fhirPackage.setMeasurePackage(getBundlePreferredFormat(measureBundle));
        }
        else {
            //fail
            packagingOutcome.setPackagingMessage("Failed Measure Bundle Validation");
            fhirPackage.setPackagingOutcome(packagingOutcome);
            return fhirPackage;
        }
        
        //write results to disk for later
        writeLibraryBundle(fhirPackage.getLibraryPackage());
        writeMeasureBundle(fhirPackage.getMeasurePackage());
        
        packagingOutcome.setName(primaryLibraryName);
        packagingOutcome.setVersion(primaryLibraryVersion);
        packagingOutcome.setResult(true);
        fhirPackage.setPackagingOutcome(packagingOutcome);
        
        return fhirPackage;
    }
    
    public FhirPackage packageFull(String id) {
        this.format = format;
        this.id = id;
        fhirPackage = new FhirPackage();
        measureBundle = new Bundle();
        libraryBundle = new Bundle();
        packagingOutcome = new FhirPackagingOutcome();
        packagingOutcome.setPackagingType("packageForBonnie");
        packagingOutcome.setBundleFormat(format);
        
        Measure measure = getMeasureResource(id);
                
        Optional<Library> optionalLibrary = null;
        for (CanonicalType library : measure.getLibrary()) {
            optionalLibrary = hapiFhirLinkProcessor.fetchLibraryByUrl(library.getValueAsString());
        }
        
        if (optionalLibrary.isPresent()) {
            Library library = optionalLibrary.get();
            primaryLibraryName = library.getName();
            primaryLibraryVersion = library.getVersion();
            library.setId(primaryLibraryName+"-"+primaryLibraryVersion);
            libraryBundle.setType(Bundle.BundleType.COLLECTION);
            libraryBundle.addEntry().setResource(library);
            createDirectory();
            writeLibraryCQLandELM(library);
            fhirIncludeLibraryResult = findIncludedFhirLibraries(library);
        }
        
        processIncludedLibraries();
        
        /*
            TODO although note required in J. Goodnough specification this is where you would
            process all codeSystems and ValueSets add them to libraryBundle
            and writing each to output directory for distribution to external systems
        */        
        
        
        boolean libraryVal = validateResource(libraryBundle, hapiFhirServer.getCtx(), "library");
        
        if (libraryVal) {
            fhirPackage.setLibraryPackage(getBundlePreferredFormat(libraryBundle));
        }
        else {
            //faile
            packagingOutcome.setPackagingMessage("Failed library Bundle Validation");
            fhirPackage.setPackagingOutcome(packagingOutcome);
            return fhirPackage;
        }
        
        
        measure.setId(primaryLibraryName+"-"+primaryLibraryVersion);
        measureBundle.setType(Bundle.BundleType.COLLECTION);
        BundleEntryComponent be = new BundleEntryComponent();
        measureBundle.addEntry().setResource(measure);
        
        boolean measureVal = validateResource(measureBundle, hapiFhirServer.getCtx(), "measure");
        
        if (measureVal) {
            fhirPackage.setMeasurePackage(getBundlePreferredFormat(measureBundle));
        }
        else {
            //fail
            packagingOutcome.setPackagingMessage("Failed Measure Bundle Validation");
            fhirPackage.setPackagingOutcome(packagingOutcome);
            return fhirPackage;
        }
        
        //write results to disk for later
        writeLibraryBundle(fhirPackage.getLibraryPackage());
        writeMeasureBundle(fhirPackage.getMeasurePackage());
        
        packagingOutcome.setName(primaryLibraryName);
        packagingOutcome.setVersion(primaryLibraryVersion);
        packagingOutcome.setResult(true);
        fhirPackage.setPackagingOutcome(packagingOutcome);
        
        return fhirPackage;
    }
    
    public byte[] packageForDistribution(String id) {
        this.id = id;
        Measure measure = getMeasureResource(id);
        
        Optional<Library> optionalLibrary = null;
        for (CanonicalType library : measure.getLibrary()) {
            optionalLibrary = hapiFhirLinkProcessor.fetchLibraryByUrl(library.getValueAsString());
        }
        if (optionalLibrary.isPresent()) {
            Library library = optionalLibrary.get();
            primaryLibraryName = library.getName();
            primaryLibraryVersion = library.getVersion();
            directoryName = PATH+primaryLibraryName+"-"+primaryLibraryVersion;
        }
        else {
            return new String("Primary Library not Found").getBytes();
        }
        
        //creating byteArray stream, make it bufforable and passing this buffor to ZipOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

        //simple file list, just for tests

        File directory = new File(directoryName);
        File[] files = directory.listFiles();

        //packing files
        try {
        for (File file : files) {
            //new zip entry and copying inputstream with file to zipOutputStream, after all closing streams
            zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
            FileInputStream fileInputStream = new FileInputStream(file);

            IOUtils.copy(fileInputStream, zipOutputStream);

            fileInputStream.close();
            zipOutputStream.closeEntry();
        }

        if (zipOutputStream != null) {
            zipOutputStream.finish();
            zipOutputStream.flush();
            IOUtils.closeQuietly(zipOutputStream);
        }
        IOUtils.closeQuietly(bufferedOutputStream);
        IOUtils.closeQuietly(byteArrayOutputStream);
        } catch (IOException ioEx) {
            
        }
        return byteArrayOutputStream.toByteArray();        
    } 
    
    public String getHumanReadible(String id) {
        this.id = id;
        Measure measure = hapiFhirServer.fetchHapiMeasure(id)
                .orElseThrow(() -> new HapiResourceNotFoundException(id, "Measure"));
        
        String humanReadible = measure.getText().toString();
        return humanReadible;
    }    
    
    private String getBundlePreferredFormat(Bundle bundle) {
        String res = "";
        if (format.equalsIgnoreCase("json")) {
            res = hapiFhirServer.toJson(bundle);
        } else if (format.equalsIgnoreCase("xml")) {
            res =  hapiFhirServer.toXml(bundle);
        }        
        return res;
    }
    
    private boolean validateResource(IBaseResource resource, FhirContext ctx, String type) {
        ca.uhn.fhir.validation.FhirValidator validator = ctx.newValidator();

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);
        instanceValidator.setNoTerminologyChecks(true);

        ValidationOptions options = new ValidationOptions();


//        if (type.equals("measure"))
//           options.addProfile("http://build.fhir.org/ig/HL7/cqf-measures/branches/R4_Lift/StructureDefinition-measure-bundle-cqfm.json");
//
//        if (type.equals("library"))
//             options.addProfile("http://build.fhir.org/ig/HL7/cqf-measures/branches/R4_Lift/StructureDefinition-library-bundle-cqfm.json");


        ValidationResult validationResult = validator.validateWithResult(resource, options);

        return validationResult.isSuccessful();
    }
    
    private Measure getMeasureResource(String id) {
        Measure measure = hapiFhirServer.fetchHapiMeasure(id)
                .orElseThrow(() -> new HapiResourceNotFoundException(id, "Measure"));     
        return measure;
    }
    
    private FhirIncludeLibraryResult findIncludedFhirLibraries(Library library) {
        List<Attachment> attachments = library.getContent();
        String name = library.getName();
        String version = library.getVersion();

        if (CollectionUtils.isEmpty(attachments)) {
            throw new CqlLibraryNotFoundException("Cannot find attachments for library name: " + name + ", version: " + version);
        }

        Attachment cql = attachments.stream()
                .filter(a -> a.getContentType().equals(CQL_CONTENT_TYPE))
                .findFirst()
                .orElseThrow(() -> new CqlLibraryNotFoundException("Cannot find attachment type " + CQL_CONTENT_TYPE +
                        " for library  name: " + name + ", version: " + version));
        byte[] bytes = Base64.decodeBase64(cql.getData());
        String cqlContent = new String(bytes);

        return fhirIncludeLibraryProcessor.findIncludedFhirLibraries(cqlContent);
    }
    
    private boolean processIncludedLibraries() {
        boolean res = true;
        List<FhirIncludeLibraryReferences> iList = fhirIncludeLibraryResult.getLibraryReferences();
        Iterator iter = iList.iterator();
        while (iter.hasNext()) {
            FhirIncludeLibraryReferences iRef = (FhirIncludeLibraryReferences)iter.next();
            String endpoint = iRef.getReferenceEndpoint();
            Optional<Library> optionalLibrary = hapiFhirLinkProcessor.fetchLibraryByUrl(endpoint);
            if (optionalLibrary.isPresent()) {
                Library library = optionalLibrary.get();
                String libraryName = library.getName();
                String libraryVersion = library.getVersion();
                library.setId(libraryName+"-"+libraryVersion);

                libraryBundle.addEntry().setResource(library);
                writeLibraryCQLandELM(library);
            }
            else {
                packagingOutcome.setPackagingMessage("Failed to find included libraries");
                res = false;
                break;
            }
        }
        
        return res;
    }
    
    private boolean writeLibraryCQLandELM(Library library) {
        boolean res = true;
        File cqlFile = new File(directoryName+library.getId()+".cql");
        File elmFile = new File(directoryName+library.getId()+".elm");
        String name = library.getName();
        String version = library.getVersion();
        
        List<Attachment> attachments = library.getContent();
        Attachment cql = attachments.stream()
                .filter(a -> a.getContentType().equals(CQL_CONTENT_TYPE))
                .findFirst()
                .orElseThrow(() -> new CqlLibraryNotFoundException("Cannot find attachment type " + CQL_CONTENT_TYPE +
                        " for library  name: " + name + ", version: " + version));
        byte[] cqlBytes = Base64.decodeBase64(cql.getData());
        String cqlText = new String(cqlBytes);
        
        attachments = library.getContent();
        Attachment elm = attachments.stream()
                .filter(a -> a.getContentType().equals(ELM_CONTENT_TYPE))
                .findFirst()
                .orElseThrow(() -> new CqlLibraryNotFoundException("Cannot find attachment type " + ELM_CONTENT_TYPE +
                        " for library  name: " + name + ", version: " + version));
        byte[] elmBytes = Base64.decodeBase64(cql.getData());
        String elmText = new String(elmBytes);
        
        //write cql
        try {
            FileWriter fw = new FileWriter(cqlFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(cqlText);
            bw.close();
        }
        catch (IOException ex) {
            log.error("Failed to write cql "+name+"-"+version);
            res = false;
        }
        
        //write elm
        try {
            FileWriter fw = new FileWriter(elmFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(elmText);
            bw.close();
        }
        catch (IOException ex) {
            log.error("Failed to write elm "+name+"-"+version);
            res = false;
        } 
        return res;
    }
    
    
    private boolean writeMeasureBundle(String bundle) {
        boolean res = true;
        File measureBundleFile = new File(directoryName+primaryLibraryName+"-"+primaryLibraryVersion+"-measure-bundle."+format);
        try {
            FileWriter fw = new FileWriter(measureBundleFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(bundle);
            bw.close();
        }
        catch (Exception ex) {
            log.error("Failed to write "+measureBundleFile.getName());
            res = false;
        }
        return res;
        
    }
    
    private boolean writeLibraryBundle(String bundle) {
        boolean res = true;
        File libraryBundleFile = new File(directoryName+primaryLibraryName+"-"+primaryLibraryVersion+"-library-bundle."+format);
        try {
            FileWriter fw = new FileWriter(libraryBundleFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(bundle);
            bw.close();
        }
        catch (Exception ex) {
            log.error("Failed to write "+libraryBundleFile.getName());
            res = false;
        }
        return res;
    }
    
    private boolean createDirectory() {
        boolean res = false;
        directoryName = PATH+primaryLibraryName+"-"+primaryLibraryVersion+"/";  
        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdir();
            res = true;
        }
        return res;
    }
   
}
