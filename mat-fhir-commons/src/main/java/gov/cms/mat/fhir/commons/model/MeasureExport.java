/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "MEASURE_EXPORT")
@XmlRootElement
public class MeasureExport implements Serializable {

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
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Measure measureId;

    public MeasureExport() {
    }

    public MeasureExport(String measureExportId) {
        this.measureExportId = measureExportId;
    }

    public MeasureExport(String measureExportId, byte[] simpleXml) {
        this.measureExportId = measureExportId;
        this.simpleXml = simpleXml;
    }

    @XmlElement
    public String getMeasureExportId() {
        return measureExportId;
    }

    public void setMeasureExportId(String measureExportId) {
        this.measureExportId = measureExportId;
    }

    @XmlElement
    public byte[] getSimpleXml() {
        return simpleXml;
    }

    public void setSimpleXml(byte[] simpleXml) {
        this.simpleXml = simpleXml;
    }

    @XmlElement
    public byte[] getCodeList() {
        return codeList;
    }

    public void setCodeList(byte[] codeList) {
        this.codeList = codeList;
    }

    @XmlElement
    public byte[] getHumanReadable() {
        return humanReadable;
    }

    public void setHumanReadable(byte[] humanReadable) {
        this.humanReadable = humanReadable;
    }

    @XmlElement
    public byte[] getHqmf() {
        return hqmf;
    }

    public void setHqmf(byte[] hqmf) {
        this.hqmf = hqmf;
    }

    @XmlElement
    public byte[] getCql() {
        return cql;
    }

    public void setCql(byte[] cql) {
        this.cql = cql;
    }

    @XmlElement
    public byte[] getElm() {
        return elm;
    }

    public void setElm(byte[] elm) {
        this.elm = elm;
    }

    @XmlElement
    public byte[] getJson() {
        return json;
    }

    public void setJson(byte[] json) {
        this.json = json;
    }

    @XmlElement
    public Measure getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Measure measureId) {
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
        return "gov.cms.mat.qdm.qicore.commons.model.MeasureExport[ measureExportId=" + measureExportId + " ]";
    }
    
}
