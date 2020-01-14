package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "SHARE_LEVEL")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ShareLevel.findAll", query = "SELECT s FROM ShareLevel s"),
    @NamedQuery(name = "ShareLevel.findByShareLevelId", query = "SELECT s FROM ShareLevel s WHERE s.shareLevelId = :shareLevelId"),
    @NamedQuery(name = "ShareLevel.findByDescription", query = "SELECT s FROM ShareLevel s WHERE s.description = :description")})
public class ShareLevel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "SHARE_LEVEL_ID")
    private String shareLevelId;
    @Column(name = "DESCRIPTION")
    private String description;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shareLevelId")
    private Collection<MeasureShare> measureShareCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shareLevelId")
    private Collection<CqlLibraryShare> cqlLibraryShareCollection;

    public ShareLevel() {
    }

    public ShareLevel(String shareLevelId) {
        this.shareLevelId = shareLevelId;
    }

    public String getShareLevelId() {
        return shareLevelId;
    }

    public void setShareLevelId(String shareLevelId) {
        this.shareLevelId = shareLevelId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    public Collection<MeasureShare> getMeasureShareCollection() {
        return measureShareCollection;
    }

    public void setMeasureShareCollection(Collection<MeasureShare> measureShareCollection) {
        this.measureShareCollection = measureShareCollection;
    }

    @XmlTransient
    public Collection<CqlLibraryShare> getCqlLibraryShareCollection() {
        return cqlLibraryShareCollection;
    }

    public void setCqlLibraryShareCollection(Collection<CqlLibraryShare> cqlLibraryShareCollection) {
        this.cqlLibraryShareCollection = cqlLibraryShareCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (shareLevelId != null ? shareLevelId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ShareLevel)) {
            return false;
        }
        ShareLevel other = (ShareLevel) object;
        if ((this.shareLevelId == null && other.shareLevelId != null) || (this.shareLevelId != null && !this.shareLevelId.equals(other.shareLevelId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.ShareLevel[ shareLevelId=" + shareLevelId + " ]";
    }
    
}
