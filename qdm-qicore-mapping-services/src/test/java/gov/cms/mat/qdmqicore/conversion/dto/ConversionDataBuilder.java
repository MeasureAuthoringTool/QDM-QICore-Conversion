package gov.cms.mat.qdmqicore.conversion.dto;

import gov.cms.mat.fhir.rest.dto.ConversionMapping;
import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.ConversionData;
import gov.cms.mat.qdmqicore.conversion.spread_sheet_data.ConversionEntry;

import java.util.ArrayList;

public interface ConversionDataBuilder {
    default ConversionMapping buildConversionMapping() {
        return ConversionMapping.builder()
                .title("title")
                .matDataTypeDescription("matDataTypeDescription")
                .matAttributeName("matAttributeName")
                .fhirR4QiCoreMapping("fhirR4QiCoreMapping")
                .fhirResource("fhirResource")
                .fhirElement("fhirElement")
                .fhirType("fhirType")
                .build();
    }

    default ConversionEntry createConversionEntryWithData(ConversionMapping conversionMapping) {
        ConversionEntry conversionEntry = createConversionEntry(conversionMapping);
        createConversionData(conversionMapping);
        return conversionEntry;
    }

    default ConversionData createConversionData(ConversionMapping conversionMapping) {
        ConversionData conversionData = new ConversionData();
        conversionData.setFeed(new ConversionData.Feed());
        conversionData.getFeed().setEntry(new ArrayList<>());
        conversionData.getFeed().getEntry().add(createConversionEntry(conversionMapping));
        return conversionData;
    }

    default ConversionEntry createConversionEntry(ConversionMapping conversionMapping) {
        ConversionEntry conversionEntry = new ConversionEntry();

        conversionEntry.setTitle(createCell(conversionMapping.getTitle()));
        conversionEntry.setMatDataTypeDescription(createCell(conversionMapping.getMatDataTypeDescription()));
        conversionEntry.setMatAttributeName(createCell(conversionMapping.getMatAttributeName()));
        conversionEntry.setFhirR4QiCoreMapping(createCell(conversionMapping.getFhirR4QiCoreMapping()));
        conversionEntry.setFhirResource(createCell(conversionMapping.getFhirResource()));
        conversionEntry.setFhirElement(createCell(conversionMapping.getFhirElement()));
        conversionEntry.setFhirType(createCell(conversionMapping.getFhirType()));

        return conversionEntry;
    }

    default ConversionEntry.Cell createCell(String data) {
        ConversionEntry.Cell cell = new ConversionEntry.Cell();
        cell.setData(data);
        return cell;
    }
}
