package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "OBJECT_STATUS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObjectStatus.findAll", query = "SELECT o FROM ObjectStatus o"),
    @NamedQuery(name = "ObjectStatus.findByObjectStatusId", query = "SELECT o FROM ObjectStatus o WHERE o.objectStatusId = :objectStatusId"),
    @NamedQuery(name = "ObjectStatus.findByDescription", query = "SELECT o FROM ObjectStatus o WHERE o.description = :description")})
public class ObjectStatus implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "OBJECT_STATUS_ID")
    private String objectStatusId;
    @Basic(optional = false)
    @Column(name = "DESCRIPTION")
    private String description;

    public ObjectStatus() {
    }

    public ObjectStatus(String objectStatusId) {
        this.objectStatusId = objectStatusId;
    }

    public ObjectStatus(String objectStatusId, String description) {
        this.objectStatusId = objectStatusId;
        this.description = description;
    }

    public String getObjectStatusId() {
        return objectStatusId;
    }

    public void setObjectStatusId(String objectStatusId) {
        this.objectStatusId = objectStatusId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (objectStatusId != null ? objectStatusId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObjectStatus)) {
            return false;
        }
        ObjectStatus other = (ObjectStatus) object;
        if ((this.objectStatusId == null && other.objectStatusId != null) || (this.objectStatusId != null && !this.objectStatusId.equals(other.objectStatusId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.ObjectStatus[ objectStatusId=" + objectStatusId + " ]";
    }
    
}
