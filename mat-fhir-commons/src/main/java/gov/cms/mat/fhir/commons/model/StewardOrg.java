package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "STEWARD_ORG")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "StewardOrg.findAll", query = "SELECT s FROM StewardOrg s"),
    @NamedQuery(name = "StewardOrg.findById", query = "SELECT s FROM StewardOrg s WHERE s.id = :id"),
    @NamedQuery(name = "StewardOrg.findByOrgName", query = "SELECT s FROM StewardOrg s WHERE s.orgName = :orgName"),
    @NamedQuery(name = "StewardOrg.findByOrgOid", query = "SELECT s FROM StewardOrg s WHERE s.orgOid = :orgOid")})
public class StewardOrg implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "ORG_NAME")
    private String orgName;
    @Column(name = "ORG_OID")
    private String orgOid;
    @OneToMany(mappedBy = "steward")
    private Collection<ListObject> listObjectCollection;

    public StewardOrg() {
    }

    public StewardOrg(String id) {
        this.id = id;
    }

    public StewardOrg(String id, String orgName) {
        this.id = id;
        this.orgName = orgName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgOid() {
        return orgOid;
    }

    public void setOrgOid(String orgOid) {
        this.orgOid = orgOid;
    }

    @XmlTransient
    public Collection<ListObject> getListObjectCollection() {
        return listObjectCollection;
    }

    public void setListObjectCollection(Collection<ListObject> listObjectCollection) {
        this.listObjectCollection = listObjectCollection;
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
        if (!(object instanceof StewardOrg)) {
            return false;
        }
        StewardOrg other = (StewardOrg) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.StewardOrg[ id=" + id + " ]";
    }
    
}
