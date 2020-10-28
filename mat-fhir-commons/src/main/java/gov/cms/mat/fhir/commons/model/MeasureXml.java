package gov.cms.mat.fhir.commons.model;

import lombok.ToString;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "MEASURE_XML")
@XmlRootElement
@ToString
public class MeasureXml implements Serializable, MatXmlBytes {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Lob
    @Column(name = "MEASURE_XML")
    private byte[] measureXml;

    @Column(name = "MEASURE_ID")
    private String measureId;

    public MeasureXml() {
    }

    public MeasureXml(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getMeasureXml() {
        return measureXml;
    }

    public void setMeasureXml(byte[] measureXml) {
        this.measureXml = measureXml;
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
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MeasureXml)) {
            return false;
        }
        MeasureXml other = (MeasureXml) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public byte[] getXmlBytes() {
        return measureXml;
    }
}
