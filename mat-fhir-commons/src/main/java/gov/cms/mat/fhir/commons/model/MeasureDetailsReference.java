package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "MEASURE_DETAILS_REFERENCE")
@XmlRootElement
public class MeasureDetailsReference implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Lob
    @Column(name = "REFERENCE")
    private String reference;
    @Column(name = "REFERENCE_NUMBER")
    private Integer referenceNumber;
    @Column(name = "MEASURE_DETAILS_ID")
    private Integer measureDetailsId;

    public MeasureDetailsReference() {
    }

    public MeasureDetailsReference(Integer id) {
        this.id = id;
    }

    @XmlElement
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlElement
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @XmlElement
    public Integer getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(Integer referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    @XmlElement
    public Integer getMeasureDetailsId() {
        return measureDetailsId;
    }

    public void setMeasureDetailsId(Integer measureDetailsId) {
        this.measureDetailsId = measureDetailsId;
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
        if (!(object instanceof MeasureDetailsReference)) {
            return false;
        }
        MeasureDetailsReference other = (MeasureDetailsReference) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureDetailsReference[ id=" + id + " ]";
    }
    
}
