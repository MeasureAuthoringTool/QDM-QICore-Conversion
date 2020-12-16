package gov.cms.mat.fhir.commons.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "CQL_LIBRARY")
@XmlRootElement
public class CqlLibrary implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "MEASURE_ID")
    private String measureId;
    @Basic(optional = false)

    @Column(name = "LIBRARY_MODEL")
    private String libraryModel;

    @Column(name = "SET_ID")
    private String setId;

    @Column(name = "CQL_NAME")
    private String cqlName;
    @Column(name = "DRAFT")
    private Boolean draft;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "VERSION")
    private BigDecimal version;
    @Column(name = "FINALIZED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finalizedDate;
    @Column(name = "RELEASE_VERSION")
    private String releaseVersion;
    @Column(name = "LOCKED_OUT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockedOutDate;
    @Lob
    @Column(name = "CQL_XML")
    private String cqlXml;
    @Column(name = "REVISION_NUMBER")
    private Integer revisionNumber;
    @Basic(optional = false)
    @Column(name = "QDM_VERSION")
    private String qdmVersion;
    @Column(name = "LAST_MODIFIED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedOn;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "EXPERIMENTAL")
    private boolean experimental;

    @JoinColumn(name = "LIBRARY_STEWARD_ID", referencedColumnName = "ORG_ID")
    @ManyToOne
    private Organization stewardId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cqlLibraryId")
    private Collection<CqlLibraryShare> cqlLibraryShareCollection;
    @JoinColumn(name = "OWNER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User ownerId;
    @JoinColumn(name = "LOCKED_USER", referencedColumnName = "USER_ID")
    @ManyToOne
    private User lockedUser;
    @JoinColumn(name = "LAST_MODIFIED_BY", referencedColumnName = "USER_ID")
    @ManyToOne
    private User lastModifiedBy;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cqlId")
    private Collection<RecentCqlActivityLog> recentCqlActivityLogCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cqlLibraryId", fetch = FetchType.EAGER)
    private Collection<CqlLibraryExport> cqlLibraryExportCollection;
    @OneToMany(mappedBy = "libraryId")
    private Collection<CqlLibraryHistory> cqlLibraryHistoryCollection;

    public CqlLibrary() {
    }

    public CqlLibrary(String id) {
        this.id = id;
    }

    public CqlLibrary(String id, String setId, String qdmVersion) {
        this.id = id;
        this.setId = setId;
        this.qdmVersion = qdmVersion;
    }

    public String getLibraryModel() {
        return libraryModel;
    }

    public void setLibraryModel(String libraryModel) {
        this.libraryModel = libraryModel;
    }

    @XmlElement
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement
    public String getMeasureId() {
        return measureId;
    }

    public void setMeasureId(String measureId) {
        this.measureId = measureId;
    }

    @XmlElement
    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    @XmlElement
    public String getCqlName() {
        return cqlName;
    }

    public void setCqlName(String cqlName) {
        this.cqlName = cqlName;
    }

    @XmlElement
    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    @XmlElement
    public BigDecimal getVersion() {
        return version;
    }

    public void setVersion(BigDecimal version) {
        this.version = version;
    }

    @XmlElement
    public Date getFinalizedDate() {
        return finalizedDate;
    }

    public void setFinalizedDate(Date finalizedDate) {
        this.finalizedDate = finalizedDate;
    }

    @XmlElement
    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    @XmlElement
    public Date getLockedOutDate() {
        return lockedOutDate;
    }

    public void setLockedOutDate(Date lockedOutDate) {
        this.lockedOutDate = lockedOutDate;
    }

    @XmlElement
    public String getCqlXml() {
        return cqlXml;
    }

    public void setCqlXml(String cqlXml) {
        this.cqlXml = cqlXml;
    }

    @XmlElement
    public Integer getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(Integer revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    @XmlElement
    public String getQdmVersion() {
        return qdmVersion;
    }

    public void setQdmVersion(String qdmVersion) {
        this.qdmVersion = qdmVersion;
    }

    @XmlElement
    public Date getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(Date lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    @XmlTransient
    public Collection<CqlLibraryShare> getCqlLibraryShareCollection() {
        return cqlLibraryShareCollection;
    }

    public void setCqlLibraryShareCollection(Collection<CqlLibraryShare> cqlLibraryShareCollection) {
        this.cqlLibraryShareCollection = cqlLibraryShareCollection;
    }

    @XmlElement
    public User getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(User ownerId) {
        this.ownerId = ownerId;
    }

    @XmlElement
    public User getLockedUser() {
        return lockedUser;
    }

    public void setLockedUser(User lockedUser) {
        this.lockedUser = lockedUser;
    }

    @XmlElement
    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @XmlTransient
    public Collection<RecentCqlActivityLog> getRecentCqlActivityLogCollection() {
        return recentCqlActivityLogCollection;
    }

    public void setRecentCqlActivityLogCollection(Collection<RecentCqlActivityLog> recentCqlActivityLogCollection) {
        this.recentCqlActivityLogCollection = recentCqlActivityLogCollection;
    }

    @XmlTransient
    public Collection<CqlLibraryExport> getCqlLibraryExportCollection() {
        return cqlLibraryExportCollection;
    }

    public void setCqlLibraryExportCollection(Collection<CqlLibraryExport> cqlLibraryExportCollection) {
        this.cqlLibraryExportCollection = cqlLibraryExportCollection;
    }

    @XmlTransient
    public Collection<CqlLibraryHistory> getCqlLibraryHistoryCollection() {
        return cqlLibraryHistoryCollection;
    }

    public void setCqlLibraryHistoryCollection(Collection<CqlLibraryHistory> cqlLibraryHistoryCollection) {
        this.cqlLibraryHistoryCollection = cqlLibraryHistoryCollection;
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
        if (!(object instanceof CqlLibrary)) {
            return false;
        }
        CqlLibrary other = (CqlLibrary) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    public Organization getSteward() {
        return stewardId;
    }

    public void setSteward(Organization stewardId) {
        this.stewardId = stewardId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isExperimental() {
        return experimental;
    }

    public void setExperimental(boolean experimental) {
        this.experimental = experimental;
    }

    public String getMatVersionFormat() {
        String version = getVersion().toString();
        String revision = getRevisionNumber().toString();

        String[] split = version.split("\\.");

        return "" + Integer.parseInt(split[0]) + "." +
                Integer.parseInt(split[1]) + "." +
                StringUtils.leftPad(revision,3,'0');
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CqlLibrary[ id=" + id + " ]";
    }
    
}
