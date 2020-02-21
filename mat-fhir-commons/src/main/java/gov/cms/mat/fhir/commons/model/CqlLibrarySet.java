package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "CQL_LIBRARY_SET")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CqlLibrarySet.findAll", query = "SELECT c FROM CqlLibrarySet c"),
    @NamedQuery(name = "CqlLibrarySet.findById", query = "SELECT c FROM CqlLibrarySet c WHERE c.id = :id"),
    @NamedQuery(name = "CqlLibrarySet.findByName", query = "SELECT c FROM CqlLibrarySet c WHERE c.name = :name")})
public class CqlLibrarySet implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "NAME")
    private String name;

    public CqlLibrarySet() {
    }

    public CqlLibrarySet(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (!(object instanceof CqlLibrarySet)) {
            return false;
        }
        CqlLibrarySet other = (CqlLibrarySet) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CqlLibrarySet[ id=" + id + " ]";
    }
    
}
