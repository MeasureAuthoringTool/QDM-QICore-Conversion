package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "METADATA")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Metadata.findAll", query = "SELECT m FROM Metadata m"),
    @NamedQuery(name = "Metadata.findByMetadataId", query = "SELECT m FROM Metadata m WHERE m.metadataId = :metadataId"),
    @NamedQuery(name = "Metadata.findByName", query = "SELECT m FROM Metadata m WHERE m.name = :name"),
    @NamedQuery(name = "Metadata.findByValue", query = "SELECT m FROM Metadata m WHERE m.value = :value")})
public class Metadata implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "METADATA_ID")
    private String metadataId;
    @Basic(optional = false)
    @Column(name = "NAME")
    private String name;
    @Basic(optional = false)
    @Column(name = "VALUE")
    private String value;
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Measure measureId;

    public Metadata() {
    }

    public Metadata(String metadataId) {
        this.metadataId = metadataId;
    }

    public Metadata(String metadataId, String name, String value) {
        this.metadataId = metadataId;
        this.name = name;
        this.value = value;
    }

    public String getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Measure getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Measure measureId) {
        this.measureId = measureId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (metadataId != null ? metadataId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Metadata)) {
            return false;
        }
        Metadata other = (Metadata) object;
        if ((this.metadataId == null && other.metadataId != null) || (this.metadataId != null && !this.metadataId.equals(other.metadataId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Metadata[ metadataId=" + metadataId + " ]";
    }
    
}
