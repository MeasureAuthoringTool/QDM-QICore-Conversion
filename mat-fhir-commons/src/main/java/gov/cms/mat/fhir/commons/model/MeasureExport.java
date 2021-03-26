package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "MEASURE_EXPORT")

public class MeasureExport implements Serializable, MatXmlBytes {

    @Lob
    @Column(name = "CODE_LIST")
    private byte[] codeList;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "MEASURE_EXPORT_ID")
    private String measureExportId;
    @Basic(optional = false)
    @Lob
    @Column(name = "SIMPLE_XML")
    private byte[] simpleXml;
    @Lob
    @Column(name = "HUMAN_READABLE")
    private byte[] humanReadable;
    @Lob
    @Column(name = "HQMF")
    private byte[] hqmf;
    @Lob
    @Column(name = "CQL")
    private byte[] cql;
    @Lob
    @Column(name = "ELM")
    private byte[] elm;
    @Lob
    @Column(name = "JSON")
    private byte[] json;
    @Column(name = "MEASURE_ID")
    private String measureId;
    @Column(name = "FHIR_LIBS_JSON")
    private byte[] fhirLibsJson;

    public MeasureExport() {
    }

    public MeasureExport(String measureExportId) {
        this.measureExportId = measureExportId;
    }

    public MeasureExport(String measureExportId, byte[] simpleXml) {
        this.measureExportId = measureExportId;
        this.simpleXml = simpleXml;
    }

    public byte[] getFhirLibsJson() {
        return fhirLibsJson;
    }

    public void setFhirLibsJson(byte[] fhirLibsJson) {
        this.fhirLibsJson = fhirLibsJson;
    }

    public String getMeasureExportId() {
        return measureExportId;
    }

    public void setMeasureExportId(String measureExportId) {
        this.measureExportId = measureExportId;
    }


    public byte[] getSimpleXml() {
        return simpleXml;
    }

    public void setSimpleXml(byte[] simpleXml) {
        this.simpleXml = simpleXml;
    }


    public byte[] getCodeList() {
        return codeList;
    }

    public void setCodeList(byte[] codeList) {
        this.codeList = codeList;
    }


    public byte[] getHumanReadable() {
        return humanReadable;
    }

    public void setHumanReadable(byte[] humanReadable) {
        this.humanReadable = humanReadable;
    }


    public byte[] getHqmf() {
        return hqmf;
    }

    public void setHqmf(byte[] hqmf) {
        this.hqmf = hqmf;
    }


    public byte[] getCql() {
        return cql;
    }

    public void setCql(byte[] cql) {
        this.cql = cql;
    }


    public byte[] getElm() {
        return elm;
    }

    public void setElm(byte[] elm) {
        this.elm = elm;
    }


    public byte[] getJson() {
        return json;
    }

    public void setJson(byte[] json) {
        this.json = json;
    }


    public String getMeasureId() {
        return measureId;
    }

    public void setMeasureId(String measureId) {
        this.measureId = measureId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (measureExportId != null ? measureExportId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MeasureExport)) {
            return false;
        }
        MeasureExport other = (MeasureExport) object;
        if ((this.measureExportId == null && other.measureExportId != null) || (this.measureExportId != null && !this.measureExportId.equals(other.measureExportId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureExport[ measureExportId=" + measureExportId + " ]";
    }

    @Override
    public byte[] getXmlBytes() {
        return simpleXml;
    }
}
