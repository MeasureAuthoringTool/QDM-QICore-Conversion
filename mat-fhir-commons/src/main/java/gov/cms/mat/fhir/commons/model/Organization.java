package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;


import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "ORGANIZATION")

@NamedQueries({
        @NamedQuery(name = "Organization.findAll", query = "SELECT o FROM Organization o"),
        @NamedQuery(name = "Organization.findByOrgId", query = "SELECT o FROM Organization o WHERE o.orgId = :orgId"),
        @NamedQuery(name = "Organization.findByOrgName", query = "SELECT o FROM Organization o WHERE o.orgName = :orgName"),
        @NamedQuery(name = "Organization.findByOrgOid", query = "SELECT o FROM Organization o WHERE o.orgOid = :orgOid")})
public class Organization implements Serializable {

    @OneToMany(mappedBy = "measureDeveloperId")
    private Collection<MeasureDeveloperAssociation> measureDeveloperAssociationCollection;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ORG_ID")
    private Integer orgId;
    @Column(name = "ORG_NAME")
    private String orgName;
    @Basic(optional = false)
    @Column(name = "ORG_OID")
    private String orgOid;
    @OneToMany(mappedBy = "measureStewardId")
    private Collection<Measure> measureCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "orgId")
    private Collection<User> userCollection;

    public Organization() {
    }

    public Organization(Integer orgId) {
        this.orgId = orgId;
    }

    public Organization(Integer orgId, String orgOid) {
        this.orgId = orgId;
        this.orgOid = orgOid;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
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


    public Collection<Measure> getMeasureCollection() {
        return measureCollection;
    }

    public void setMeasureCollection(Collection<Measure> measureCollection) {
        this.measureCollection = measureCollection;
    }


    public Collection<User> getUserCollection() {
        return userCollection;
    }

    public void setUserCollection(Collection<User> userCollection) {
        this.userCollection = userCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (orgId != null ? orgId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Organization)) {
            return false;
        }
        Organization other = (Organization) object;
        if ((this.orgId == null && other.orgId != null) || (this.orgId != null && !this.orgId.equals(other.orgId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.qdm.qicore.commons.model.Organization[ orgId=" + orgId + " ]";
    }


    public Collection<MeasureDeveloperAssociation> getMeasureDeveloperAssociationCollection() {
        return measureDeveloperAssociationCollection;
    }

    public void setMeasureDeveloperAssociationCollection(Collection<MeasureDeveloperAssociation> measureDeveloperAssociationCollection) {
        this.measureDeveloperAssociationCollection = measureDeveloperAssociationCollection;
    }

}
