package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "AUDIT_LOG")
@NamedQueries({
        @NamedQuery(name = "AuditLog.findAll", query = "SELECT a FROM AuditLog a"),
        @NamedQuery(name = "AuditLog.findByAuditLogId", query = "SELECT a FROM AuditLog a WHERE a.auditLogId = :auditLogId"),
        @NamedQuery(name = "AuditLog.findByCreateDate", query = "SELECT a FROM AuditLog a WHERE a.createDate = :createDate"),
        @NamedQuery(name = "AuditLog.findByUpdateDate", query = "SELECT a FROM AuditLog a WHERE a.updateDate = :updateDate"),
        @NamedQuery(name = "AuditLog.findByActivityType", query = "SELECT a FROM AuditLog a WHERE a.activityType = :activityType"),
        @NamedQuery(name = "AuditLog.findByMeasureId", query = "SELECT a FROM AuditLog a WHERE a.measureId = :measureId"),
        @NamedQuery(name = "AuditLog.findByListObjectId", query = "SELECT a FROM AuditLog a WHERE a.listObjectId = :listObjectId"),
        @NamedQuery(name = "AuditLog.findByClauseId", query = "SELECT a FROM AuditLog a WHERE a.clauseId = :clauseId"),
        @NamedQuery(name = "AuditLog.findByQdmId", query = "SELECT a FROM AuditLog a WHERE a.qdmId = :qdmId")})
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "AUDIT_LOG_ID")
    private String auditLogId;
    @Basic(optional = false)
    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @Column(name = "UPDATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;
    @Column(name = "ACTIVITY_TYPE")
    private String activityType;
    @Column(name = "MEASURE_ID")
    private String measureId;
    @Column(name = "LIST_OBJECT_ID")
    private String listObjectId;
    @Column(name = "CLAUSE_ID")
    private String clauseId;
    @Column(name = "QDM_ID")
    private String qdmId;
    @JoinColumn(name = "CREATE_USER", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User createUser;
    @JoinColumn(name = "UPDATE_USER", referencedColumnName = "USER_ID")
    @ManyToOne
    private User updateUser;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "auditId")
    private Collection<User> userCollection;

    public AuditLog() {
    }

    public AuditLog(String auditLogId) {
        this.auditLogId = auditLogId;
    }

    public AuditLog(String auditLogId, Date createDate) {
        this.auditLogId = auditLogId;
        this.createDate = createDate;
    }

    public String getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(String auditLogId) {
        this.auditLogId = auditLogId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getMeasureId() {
        return measureId;
    }

    public void setMeasureId(String measureId) {
        this.measureId = measureId;
    }

    public String getListObjectId() {
        return listObjectId;
    }

    public void setListObjectId(String listObjectId) {
        this.listObjectId = listObjectId;
    }

    public String getClauseId() {
        return clauseId;
    }

    public void setClauseId(String clauseId) {
        this.clauseId = clauseId;
    }

    public String getQdmId() {
        return qdmId;
    }

    public void setQdmId(String qdmId) {
        this.qdmId = qdmId;
    }

    public User getCreateUser() {
        return createUser;
    }

    public void setCreateUser(User createUser) {
        this.createUser = createUser;
    }

    public User getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(User updateUser) {
        this.updateUser = updateUser;
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
        hash += (auditLogId != null ? auditLogId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AuditLog)) {
            return false;
        }
        AuditLog other = (AuditLog) object;
        if ((this.auditLogId == null && other.auditLogId != null) || (this.auditLogId != null && !this.auditLogId.equals(other.auditLogId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.qdm.qicore.commons.model.AuditLog[ auditLogId=" + auditLogId + " ]";
    }

}
