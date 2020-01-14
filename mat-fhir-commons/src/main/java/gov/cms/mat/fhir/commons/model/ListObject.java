package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "LIST_OBJECT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ListObject.findAll", query = "SELECT l FROM ListObject l"),
    @NamedQuery(name = "ListObject.findByListObjectId", query = "SELECT l FROM ListObject l WHERE l.listObjectId = :listObjectId"),
    @NamedQuery(name = "ListObject.findByName", query = "SELECT l FROM ListObject l WHERE l.name = :name"),
    @NamedQuery(name = "ListObject.findByOid", query = "SELECT l FROM ListObject l WHERE l.oid = :oid"),
    @NamedQuery(name = "ListObject.findByRationale", query = "SELECT l FROM ListObject l WHERE l.rationale = :rationale"),
    @NamedQuery(name = "ListObject.findByComment", query = "SELECT l FROM ListObject l WHERE l.comment = :comment"),
    @NamedQuery(name = "ListObject.findByCodeSysVersion", query = "SELECT l FROM ListObject l WHERE l.codeSysVersion = :codeSysVersion"),
    @NamedQuery(name = "ListObject.findByStewardOther", query = "SELECT l FROM ListObject l WHERE l.stewardOther = :stewardOther"),
    @NamedQuery(name = "ListObject.findByCodeListDeveloper", query = "SELECT l FROM ListObject l WHERE l.codeListDeveloper = :codeListDeveloper"),
    @NamedQuery(name = "ListObject.findByCodeListContext", query = "SELECT l FROM ListObject l WHERE l.codeListContext = :codeListContext"),
    @NamedQuery(name = "ListObject.findByLastModified", query = "SELECT l FROM ListObject l WHERE l.lastModified = :lastModified"),
    @NamedQuery(name = "ListObject.findByDraft", query = "SELECT l FROM ListObject l WHERE l.draft = :draft")})
public class ListObject implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "LIST_OBJECT_ID")
    private String listObjectId;
    @Basic(optional = false)
    @Column(name = "NAME")
    private String name;
    @Basic(optional = false)
    @Column(name = "OID")
    private String oid;
    @Basic(optional = false)
    @Column(name = "RATIONALE")
    private String rationale;
    @Column(name = "COMMENT")
    private String comment;
    @Basic(optional = false)
    @Column(name = "CODE_SYS_VERSION")
    private String codeSysVersion;
    @Column(name = "STEWARD_OTHER")
    private String stewardOther;
    @Column(name = "CODE_LIST_DEVELOPER")
    private String codeListDeveloper;
    @Column(name = "CODE_LIST_CONTEXT")
    private String codeListContext;
    @Column(name = "LAST_MODIFIED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;
    @Basic(optional = false)
    @Column(name = "DRAFT")
    private boolean draft;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "listObject")
    private CodeList codeList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "listObjectId")
    private Collection<QualityDataModel> qualityDataModelCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "groupListId")
    private Collection<GroupedCodeLists> groupedCodeListsCollection;
    @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "CATEGORY_ID")
    @ManyToOne(optional = false)
    private Category categoryId;
    @JoinColumn(name = "CODE_SYSTEM_ID", referencedColumnName = "CODE_SYSTEM_ID")
    @ManyToOne(optional = false)
    private CodeSystem codeSystemId;
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne
    private Measure measureId;
    @JoinColumn(name = "STEWARD", referencedColumnName = "ID")
    @ManyToOne
    private StewardOrg steward;
    @JoinColumn(name = "OBJECT_OWNER", referencedColumnName = "USER_ID")
    @ManyToOne
    private User objectOwner;

    public ListObject() {
    }

    public ListObject(String listObjectId) {
        this.listObjectId = listObjectId;
    }

    public ListObject(String listObjectId, String name, String oid, String rationale, String codeSysVersion, boolean draft) {
        this.listObjectId = listObjectId;
        this.name = name;
        this.oid = oid;
        this.rationale = rationale;
        this.codeSysVersion = codeSysVersion;
        this.draft = draft;
    }

    public String getListObjectId() {
        return listObjectId;
    }

    public void setListObjectId(String listObjectId) {
        this.listObjectId = listObjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCodeSysVersion() {
        return codeSysVersion;
    }

    public void setCodeSysVersion(String codeSysVersion) {
        this.codeSysVersion = codeSysVersion;
    }

    public String getStewardOther() {
        return stewardOther;
    }

    public void setStewardOther(String stewardOther) {
        this.stewardOther = stewardOther;
    }

    public String getCodeListDeveloper() {
        return codeListDeveloper;
    }

    public void setCodeListDeveloper(String codeListDeveloper) {
        this.codeListDeveloper = codeListDeveloper;
    }

    public String getCodeListContext() {
        return codeListContext;
    }

    public void setCodeListContext(String codeListContext) {
        this.codeListContext = codeListContext;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean getDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public CodeList getCodeList() {
        return codeList;
    }

    public void setCodeList(CodeList codeList) {
        this.codeList = codeList;
    }

    @XmlTransient
    public Collection<QualityDataModel> getQualityDataModelCollection() {
        return qualityDataModelCollection;
    }

    public void setQualityDataModelCollection(Collection<QualityDataModel> qualityDataModelCollection) {
        this.qualityDataModelCollection = qualityDataModelCollection;
    }

    @XmlTransient
    public Collection<GroupedCodeLists> getGroupedCodeListsCollection() {
        return groupedCodeListsCollection;
    }

    public void setGroupedCodeListsCollection(Collection<GroupedCodeLists> groupedCodeListsCollection) {
        this.groupedCodeListsCollection = groupedCodeListsCollection;
    }

    public Category getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
    }

    public CodeSystem getCodeSystemId() {
        return codeSystemId;
    }

    public void setCodeSystemId(CodeSystem codeSystemId) {
        this.codeSystemId = codeSystemId;
    }

    public Measure getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Measure measureId) {
        this.measureId = measureId;
    }

    public StewardOrg getSteward() {
        return steward;
    }

    public void setSteward(StewardOrg steward) {
        this.steward = steward;
    }

    public User getObjectOwner() {
        return objectOwner;
    }

    public void setObjectOwner(User objectOwner) {
        this.objectOwner = objectOwner;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (listObjectId != null ? listObjectId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ListObject)) {
            return false;
        }
        ListObject other = (ListObject) object;
        if ((this.listObjectId == null && other.listObjectId != null) || (this.listObjectId != null && !this.listObjectId.equals(other.listObjectId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.ListObject[ listObjectId=" + listObjectId + " ]";
    }
    
}
