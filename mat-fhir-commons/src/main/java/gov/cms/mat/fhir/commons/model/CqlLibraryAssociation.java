package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;


@Entity
@Table(name = "CQL_LIBRARY_ASSOCIATION")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CqlLibraryAssociation.findAll", query = "SELECT c FROM CqlLibraryAssociation c"),
    @NamedQuery(name = "CqlLibraryAssociation.findById", query = "SELECT c FROM CqlLibraryAssociation c WHERE c.id = :id"),
    @NamedQuery(name = "CqlLibraryAssociation.findByAssociationId", query = "SELECT c FROM CqlLibraryAssociation c WHERE c.associationId = :associationId"),
    @NamedQuery(name = "CqlLibraryAssociation.findByCqlLibraryId", query = "SELECT c FROM CqlLibraryAssociation c WHERE c.cqlLibraryId = :cqlLibraryId")})
public class CqlLibraryAssociation implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "ASSOCIATION_ID")
    private String associationId;
    @Basic(optional = false)
    @Column(name = "CQL_LIBRARY_ID")
    private String cqlLibraryId;

    public CqlLibraryAssociation() {
    }

    public CqlLibraryAssociation(String id) {
        this.id = id;
    }

    public CqlLibraryAssociation(String id, String associationId, String cqlLibraryId) {
        this.id = id;
        this.associationId = associationId;
        this.cqlLibraryId = cqlLibraryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssociationId() {
        return associationId;
    }

    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    public String getCqlLibraryId() {
        return cqlLibraryId;
    }

    public void setCqlLibraryId(String cqlLibraryId) {
        this.cqlLibraryId = cqlLibraryId;
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
        if (!(object instanceof CqlLibraryAssociation)) {
            return false;
        }
        CqlLibraryAssociation other = (CqlLibraryAssociation) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CqlLibraryAssociation[ id=" + id + " ]";
    }
    
}
