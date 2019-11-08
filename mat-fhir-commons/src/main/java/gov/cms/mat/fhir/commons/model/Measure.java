/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "MEASURE")
@XmlRootElement
public class Measure implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<MeasureDeveloperAssociation> measureDeveloperAssociationCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<Packager> packagerCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "componentMeasureId")
    private Collection<ComponentMeasures> componentMeasuresCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "compositeMeasureId")
    private Collection<ComponentMeasures> componentMeasuresCollection1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<RecentMsrActivityLog> recentMsrActivityLogCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<MeasureShare> measureShareCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<MeasureDetails> measureDetailsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<MeasureXml> measureXmlCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<QualityDataModel> qualityDataModelCollection;
    @OneToMany(mappedBy = "measureId")
    private Collection<Clause> clauseCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<MeasureTypeAssociation> measureTypeAssociationCollection;
    @OneToMany(mappedBy = "measureId")
    private Collection<ListObject> listObjectCollection;
    @OneToMany(mappedBy = "measureId")
    private Collection<CqlLibraryHistory> cqlLibraryHistoryCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<CqlData> cqlDataCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<Metadata> metadataCollection;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "ABBR_NAME")
    private String abbrName;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "MEASURE_STATUS")
    private String measureStatus;
    @Column(name = "EXPORT_TS")
    @Temporal(TemporalType.TIMESTAMP)
    private Date exportTs;
    @Column(name = "LOCKED_OUT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockedOutDate;
    @Basic(optional = false)
    @Column(name = "SCORING")
    private String scoring;
    @Column(name = "FINALIZED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finalizedDate;
    @Basic(optional = false)
    @Column(name = "DRAFT")
    private boolean draft;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @Column(name = "VERSION")
    private BigDecimal version;
    @Column(name = "VALUE_SET_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date valueSetDate;
    @Column(name = "EMEASURE_ID")
    private Integer emeasureId;
    @Basic(optional = false)
    @Column(name = "PRIVATE")
    private boolean private1;
    @Column(name = "DELETED")
    private String deleted;
    @Column(name = "REVISION_NUMBER")
    private Integer revisionNumber;
    @Column(name = "RELEASE_VERSION")
    private String releaseVersion;
    @Column(name = "LAST_MODIFIED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedOn;
    @Column(name = "PATIENT_BASED")
    private Boolean patientBased;
    @Column(name = "QDM_VERSION")
    private String qdmVersion;
    @Basic(optional = false)
    @Column(name = "IS_COMPOSITE_MEASURE")
    private boolean isCompositeMeasure;
    @Column(name = "COMPOSITE_SCORING")
    private String compositeScoring;
    @Lob
    @Column(name = "NQF_NUMBER")
    private byte[] nqfNumber;
    @Column(name = "MEASUREMENT_PERIOD_FROM")
    @Temporal(TemporalType.TIMESTAMP)
    private Date measurementPeriodFrom;
    @Column(name = "MEASUREMENT_PERIOD_TO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date measurementPeriodTo;
    @Column(name = "CQL_NAME")
    private String cqlName;
    @JoinColumn(name = "LOCKED_USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne
    private User lockedUserId;
    @JoinColumn(name = "MEASURE_OWNER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User measureOwnerId;
    @JoinColumn(name = "MEASURE_SET_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private MeasureSet measureSetId;
    @JoinColumn(name = "LAST_MODIFIED_BY", referencedColumnName = "USER_ID")
    @ManyToOne
    private User lastModifiedBy;
    @JoinColumn(name = "MEASURE_STEWARD_ID", referencedColumnName = "ORG_ID")
    @ManyToOne
    private Organization measureStewardId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureId")
    private Collection<MeasureExport> measureExportCollection;

    public Measure() {
    }

    public Measure(String id) {
        this.id = id;
    }

    public Measure(String id, String scoring, boolean draft, BigDecimal version, boolean private1, boolean isCompositeMeasure) {
        this.id = id;
        this.scoring = scoring;
        this.draft = draft;
        this.version = version;
        this.private1 = private1;
        this.isCompositeMeasure = isCompositeMeasure;
    }

    @XmlElement
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement
    public String getAbbrName() {
        return abbrName;
    }

    public void setAbbrName(String abbrName) {
        this.abbrName = abbrName;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    public String getMeasureStatus() {
        return measureStatus;
    }

    public void setMeasureStatus(String measureStatus) {
        this.measureStatus = measureStatus;
    }

    @XmlElement
    public Date getExportTs() {
        return exportTs;
    }

    public void setExportTs(Date exportTs) {
        this.exportTs = exportTs;
    }

    @XmlElement
    public Date getLockedOutDate() {
        return lockedOutDate;
    }

    public void setLockedOutDate(Date lockedOutDate) {
        this.lockedOutDate = lockedOutDate;
    }

    @XmlElement
    public String getScoring() {
        return scoring;
    }

    public void setScoring(String scoring) {
        this.scoring = scoring;
    }

    @XmlElement
    public Date getFinalizedDate() {
        return finalizedDate;
    }

    public void setFinalizedDate(Date finalizedDate) {
        this.finalizedDate = finalizedDate;
    }

    @XmlElement
    public boolean getDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
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
    public Date getValueSetDate() {
        return valueSetDate;
    }

    public void setValueSetDate(Date valueSetDate) {
        this.valueSetDate = valueSetDate;
    }

    @XmlElement
    public Integer getEmeasureId() {
        return emeasureId;
    }

    public void setEmeasureId(Integer emeasureId) {
        this.emeasureId = emeasureId;
    }

    @XmlElement
    public boolean getPrivate1() {
        return private1;
    }

    public void setPrivate1(boolean private1) {
        this.private1 = private1;
    }

    @XmlElement
    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    @XmlElement
    public Integer getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(Integer revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    @XmlElement
    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    @XmlElement
    public Date getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(Date lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    @XmlElement
    public Boolean getPatientBased() {
        return patientBased;
    }

    public void setPatientBased(Boolean patientBased) {
        this.patientBased = patientBased;
    }

    @XmlElement
    public String getQdmVersion() {
        return qdmVersion;
    }

    public void setQdmVersion(String qdmVersion) {
        this.qdmVersion = qdmVersion;
    }

    @XmlElement
    public boolean getIsCompositeMeasure() {
        return isCompositeMeasure;
    }

    
    public void setIsCompositeMeasure(boolean isCompositeMeasure) {
        this.isCompositeMeasure = isCompositeMeasure;
    }

    @XmlElement
    public String getCompositeScoring() {
        return compositeScoring;
    }

    public void setCompositeScoring(String compositeScoring) {
        this.compositeScoring = compositeScoring;
    }

    @XmlElement
    public byte[] getNqfNumber() {
        return nqfNumber;
    }

    public void setNqfNumber(byte[] nqfNumber) {
        this.nqfNumber = nqfNumber;
    }

    @XmlElement
    public Date getMeasurementPeriodFrom() {
        return measurementPeriodFrom;
    }

    public void setMeasurementPeriodFrom(Date measurementPeriodFrom) {
        this.measurementPeriodFrom = measurementPeriodFrom;
    }

    @XmlElement
    public Date getMeasurementPeriodTo() {
        return measurementPeriodTo;
    }

    public void setMeasurementPeriodTo(Date measurementPeriodTo) {
        this.measurementPeriodTo = measurementPeriodTo;
    }

    @XmlElement
    public String getCqlName() {
        return cqlName;
    }

    public void setCqlName(String cqlName) {
        this.cqlName = cqlName;
    }

    @XmlElement
    public User getLockedUserId() {
        return lockedUserId;
    }

    public void setLockedUserId(User lockedUserId) {
        this.lockedUserId = lockedUserId;
    }

    @XmlElement
    public User getMeasureOwnerId() {
        return measureOwnerId;
    }

    public void setMeasureOwnerId(User measureOwnerId) {
        this.measureOwnerId = measureOwnerId;
    }

    @XmlElement
    public MeasureSet getMeasureSetId() {
        return measureSetId;
    }

    public void setMeasureSetId(MeasureSet measureSetId) {
        this.measureSetId = measureSetId;
    }

    @XmlElement
    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @XmlElement
    public Organization getMeasureStewardId() {
        return measureStewardId;
    }

    public void setMeasureStewardId(Organization measureStewardId) {
        this.measureStewardId = measureStewardId;
    }

    @XmlTransient
    public Collection<MeasureExport> getMeasureExportCollection() {
        return measureExportCollection;
    }

    public void setMeasureExportCollection(Collection<MeasureExport> measureExportCollection) {
        this.measureExportCollection = measureExportCollection;
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
        if (!(object instanceof Measure)) {
            return false;
        }
        Measure other = (Measure) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.qdm.qicore.commons.model.Measure[ id=" + id + " ]";
    }

    @XmlTransient
    public Collection<MeasureDeveloperAssociation> getMeasureDeveloperAssociationCollection() {
        return measureDeveloperAssociationCollection;
    }

    public void setMeasureDeveloperAssociationCollection(Collection<MeasureDeveloperAssociation> measureDeveloperAssociationCollection) {
        this.measureDeveloperAssociationCollection = measureDeveloperAssociationCollection;
    }

    @XmlTransient
    public Collection<Packager> getPackagerCollection() {
        return packagerCollection;
    }

    public void setPackagerCollection(Collection<Packager> packagerCollection) {
        this.packagerCollection = packagerCollection;
    }

    @XmlTransient
    public Collection<ComponentMeasures> getComponentMeasuresCollection() {
        return componentMeasuresCollection;
    }

    public void setComponentMeasuresCollection(Collection<ComponentMeasures> componentMeasuresCollection) {
        this.componentMeasuresCollection = componentMeasuresCollection;
    }

    @XmlTransient
    public Collection<ComponentMeasures> getComponentMeasuresCollection1() {
        return componentMeasuresCollection1;
    }

    public void setComponentMeasuresCollection1(Collection<ComponentMeasures> componentMeasuresCollection1) {
        this.componentMeasuresCollection1 = componentMeasuresCollection1;
    }

    @XmlTransient
    public Collection<RecentMsrActivityLog> getRecentMsrActivityLogCollection() {
        return recentMsrActivityLogCollection;
    }

    public void setRecentMsrActivityLogCollection(Collection<RecentMsrActivityLog> recentMsrActivityLogCollection) {
        this.recentMsrActivityLogCollection = recentMsrActivityLogCollection;
    }

    @XmlTransient
    public Collection<MeasureShare> getMeasureShareCollection() {
        return measureShareCollection;
    }

    public void setMeasureShareCollection(Collection<MeasureShare> measureShareCollection) {
        this.measureShareCollection = measureShareCollection;
    }

    @XmlTransient
    public Collection<MeasureDetails> getMeasureDetailsCollection() {
        return measureDetailsCollection;
    }

    public void setMeasureDetailsCollection(Collection<MeasureDetails> measureDetailsCollection) {
        this.measureDetailsCollection = measureDetailsCollection;
    }

    @XmlTransient
    public Collection<MeasureXml> getMeasureXmlCollection() {
        return measureXmlCollection;
    }

    public void setMeasureXmlCollection(Collection<MeasureXml> measureXmlCollection) {
        this.measureXmlCollection = measureXmlCollection;
    }

    @XmlTransient
    public Collection<QualityDataModel> getQualityDataModelCollection() {
        return qualityDataModelCollection;
    }

    public void setQualityDataModelCollection(Collection<QualityDataModel> qualityDataModelCollection) {
        this.qualityDataModelCollection = qualityDataModelCollection;
    }

    @XmlTransient
    public Collection<Clause> getClauseCollection() {
        return clauseCollection;
    }

    public void setClauseCollection(Collection<Clause> clauseCollection) {
        this.clauseCollection = clauseCollection;
    }

    @XmlTransient
    public Collection<MeasureTypeAssociation> getMeasureTypeAssociationCollection() {
        return measureTypeAssociationCollection;
    }

    public void setMeasureTypeAssociationCollection(Collection<MeasureTypeAssociation> measureTypeAssociationCollection) {
        this.measureTypeAssociationCollection = measureTypeAssociationCollection;
    }

    @XmlTransient
    public Collection<ListObject> getListObjectCollection() {
        return listObjectCollection;
    }

    public void setListObjectCollection(Collection<ListObject> listObjectCollection) {
        this.listObjectCollection = listObjectCollection;
    }

    @XmlTransient
    public Collection<CqlLibraryHistory> getCqlLibraryHistoryCollection() {
        return cqlLibraryHistoryCollection;
    }

    public void setCqlLibraryHistoryCollection(Collection<CqlLibraryHistory> cqlLibraryHistoryCollection) {
        this.cqlLibraryHistoryCollection = cqlLibraryHistoryCollection;
    }

    @XmlTransient
    public Collection<CqlData> getCqlDataCollection() {
        return cqlDataCollection;
    }

    public void setCqlDataCollection(Collection<CqlData> cqlDataCollection) {
        this.cqlDataCollection = cqlDataCollection;
    }

    @XmlTransient
    public Collection<Metadata> getMetadataCollection() {
        return metadataCollection;
    }

    public void setMetadataCollection(Collection<Metadata> metadataCollection) {
        this.metadataCollection = metadataCollection;
    }

    @Transient
    public double getVersionNumber(){
        if( version == null)  {
            return 0.0;
        } else {
            return version.doubleValue();
        }


    }
    
}
