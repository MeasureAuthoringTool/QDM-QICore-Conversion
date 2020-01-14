package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "TRANSACTION_AUDIT_LOG")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TransactionAuditLog.findAll", query = "SELECT t FROM TransactionAuditLog t"),
    @NamedQuery(name = "TransactionAuditLog.findById", query = "SELECT t FROM TransactionAuditLog t WHERE t.id = :id"),
    @NamedQuery(name = "TransactionAuditLog.findByPrimaryId", query = "SELECT t FROM TransactionAuditLog t WHERE t.primaryId = :primaryId"),
    @NamedQuery(name = "TransactionAuditLog.findBySecondaryId", query = "SELECT t FROM TransactionAuditLog t WHERE t.secondaryId = :secondaryId"),
    @NamedQuery(name = "TransactionAuditLog.findByActivityType", query = "SELECT t FROM TransactionAuditLog t WHERE t.activityType = :activityType"),
    @NamedQuery(name = "TransactionAuditLog.findByUserId", query = "SELECT t FROM TransactionAuditLog t WHERE t.userId = :userId"),
    @NamedQuery(name = "TransactionAuditLog.findByTimestamp", query = "SELECT t FROM TransactionAuditLog t WHERE t.timestamp = :timestamp"),
    @NamedQuery(name = "TransactionAuditLog.findByAddlInfo", query = "SELECT t FROM TransactionAuditLog t WHERE t.addlInfo = :addlInfo")})
public class TransactionAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "PRIMARY_ID")
    private String primaryId;
    @Column(name = "SECONDARY_ID")
    private String secondaryId;
    @Basic(optional = false)
    @Column(name = "ACTIVITY_TYPE")
    private String activityType;
    @Basic(optional = false)
    @Column(name = "USER_ID")
    private String userId;
    @Basic(optional = false)
    @Column(name = "TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    @Column(name = "ADDL_INFO")
    private String addlInfo;

    public TransactionAuditLog() {
    }

    public TransactionAuditLog(String id) {
        this.id = id;
    }

    public TransactionAuditLog(String id, String activityType, String userId, Date timestamp) {
        this.id = id;
        this.activityType = activityType;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public String getSecondaryId() {
        return secondaryId;
    }

    public void setSecondaryId(String secondaryId) {
        this.secondaryId = secondaryId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAddlInfo() {
        return addlInfo;
    }

    public void setAddlInfo(String addlInfo) {
        this.addlInfo = addlInfo;
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
        if (!(object instanceof TransactionAuditLog)) {
            return false;
        }
        TransactionAuditLog other = (TransactionAuditLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.TransactionAuditLog[ id=" + id + " ]";
    }
    
}
