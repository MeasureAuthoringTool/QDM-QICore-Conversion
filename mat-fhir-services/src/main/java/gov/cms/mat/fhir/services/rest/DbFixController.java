package gov.cms.mat.fhir.services.rest;

import gov.cms.mat.fhir.commons.model.CqlLibraryExport;
import gov.cms.mat.fhir.commons.model.MeasureDetailsReference;
import gov.cms.mat.fhir.commons.model.MeasureExport;
import gov.cms.mat.fhir.commons.model.MeasureXml;
import gov.cms.mat.fhir.services.repository.CqlLibraryExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureDetailsReferenceRepository;
import gov.cms.mat.fhir.services.repository.MeasureExportRepository;
import gov.cms.mat.fhir.services.repository.MeasureXmlRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/dbFix")
@Tag(name = "Fix-Controller", description = "API for testing fixes")
@Slf4j
public class DbFixController {
    private final MeasureExportRepository measureExportRepository;
    private final MeasureXmlRepository measureXmlRepository;
    private final CqlLibraryExportRepository cqlLibraryExportRepository;
    private final MeasureDetailsReferenceRepository measureDetailsReferenceRepository;

    public DbFixController(MeasureExportRepository measureExportRepository,
                           MeasureXmlRepository measureXmlRepository,
                           CqlLibraryExportRepository cqlLibraryExportRepository,
                           MeasureDetailsReferenceRepository measureDetailsReferenceRepository) {
        this.measureExportRepository = measureExportRepository;
        this.measureXmlRepository = measureXmlRepository;
        this.cqlLibraryExportRepository = cqlLibraryExportRepository;
        this.measureDetailsReferenceRepository = measureDetailsReferenceRepository;
    }

    @GetMapping
    public List<TableResult> createTableResults() {
        List<TableResult> results = new ArrayList<>();
        results.add(processMeasureDetailsReferenceTable());
        results.add(processCqlLibraryExportTable());
        results.add(processMeasureXmlTable());
        results.add(processMeasureExportTable());

        return results;
    }

    private TableResult processMeasureDetailsReferenceTable() {
        return TableResult.builder()
                .name("MEASURE_DETAILS_REFERENCE")
                .rowResults(createRowResultsMeasureDetailsReference())
                .build();
    }

    private TableResult processCqlLibraryExportTable() {
        return TableResult.builder()
                .name("CQL_LIBRARY_EXPORT")
                .rowResults(createRowResultsCqlLibraryExport())
                .build();
    }

    private TableResult processMeasureXmlTable() {
        return TableResult.builder()
                .name("MEASURE_XML")
                .rowResults(createRowResultsMeasureXml())
                .build();
    }

    private TableResult processMeasureExportTable() {
        return TableResult.builder()
                .name("MEASURE_EXPORT")
                .rowResults(createRowResultsMeasureExport())
                .build();
    }

    private List<RowResult>   createRowResultsMeasureDetailsReference() {
        return measureDetailsReferenceRepository.findAll().stream().parallel()
                .filter(x -> x.getReference() != null)
                .map(this::createRowResultMeasureDetailsReference)
                .sorted(Comparator.comparing(RowResult::getId))
                .collect(Collectors.toList());
    }

    private List<RowResult> createRowResultsMeasureXml() {
        return measureXmlRepository.findAll().stream().parallel()
                .filter(x -> x.getSevereErrorCql() != null)
                .map(this::createRowResultMeasureXml)
                .sorted(Comparator.comparing(RowResult::getId))
                .collect(Collectors.toList());
    }

    public List<RowResult> createRowResultsCqlLibraryExport() {
        return cqlLibraryExportRepository.findAll().stream().parallel()
                .filter(export -> export.getElm() != null)
                .map(this::createRowResultCqlLibraryExport)
                .sorted(Comparator.comparing(RowResult::getId))
                .collect(Collectors.toList());
    }


    public List<RowResult> createRowResultsMeasureExport() {
        return measureExportRepository.findAll().stream().parallel()
                .filter(export -> export.getElm() != null)
                .map(this::createRowResultMeasureExport)
                .sorted(Comparator.comparing(RowResult::getId))
                .collect(Collectors.toList());
    }

    private RowResult createRowResultCqlLibraryExport(CqlLibraryExport cqlLibraryExport) {
        RowResult result = new RowResult(cqlLibraryExport.getId());

        result.getColumnResults()
                .add(createColumnResult("JSON", cqlLibraryExport.getJson()));

        result.getColumnResults()
                .add(createColumnResult("CQL", cqlLibraryExport.getCql()));

        result.getColumnResults()
                .add(createColumnResult("ELM", cqlLibraryExport.getElm()));

        return result;
    }

    private RowResult createRowResultMeasureXml(MeasureXml measureXml) {
        RowResult result = new RowResult(measureXml.getId());

        result.getColumnResults()
                .add(createColumnResult("SEVERE_ERROR_CQL", measureXml.getSevereErrorCql()));

        return result;
    }

    private RowResult createRowResultMeasureDetailsReference(MeasureDetailsReference measureXml) {
        RowResult result = new RowResult("" + measureXml.getId());

        result.getColumnResults()
                .add(createColumnResult("REFERENCE", measureXml.getReference()));

        return result;
    }

    private RowResult createRowResultMeasureExport(MeasureExport measureExport) {
        RowResult result = new RowResult(measureExport.getMeasureExportId());

        result.getColumnResults()
                .add(createColumnResult("HUMAN_READABLE", measureExport.getHumanReadable()));

        result.getColumnResults()
                .add(createColumnResult("JSON", measureExport.getJson()));

        result.getColumnResults()
                .add(createColumnResult("FHIR_LIBS_JSON", measureExport.getFhirLibsJson()));

        result.getColumnResults()
                .add(createColumnResult("SIMPLE_XML", measureExport.getSimpleXml()));

        result.getColumnResults()
                .add(createColumnResult("HQMF", measureExport.getHqmf()));

        result.getColumnResults()
                .add(createColumnResult("CQL", measureExport.getCql()));

        return result;
    }

    private ColumnResult createColumnResult(String column, byte[] bytes) {
        return buildRowResult(column, bytes == null ? null : Arrays.hashCode(bytes));
    }

    private ColumnResult createColumnResult(String column, String data) {
        return ColumnResult.builder()
                .column(column)
                .hash(data == null ? null : data.hashCode())
                .build();
    }

    private ColumnResult buildRowResult(String column, Integer hash) {
        return ColumnResult.builder()
                .column(column)
                .hash(hash)
                .build();
    }

    @Getter
    @Builder
    private static class TableResult {
        private final String name;
        private final List<RowResult> rowResults;
    }

    @Data
    private static class RowResult {
        private final String id;
        List<ColumnResult> columnResults = new ArrayList<>();
    }

    @Getter
    @Builder
    private static class ColumnResult {
        String column;
        Integer hash;
    }
}
