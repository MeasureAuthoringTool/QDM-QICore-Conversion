package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "CQL_AUDIT_LOG")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CqlAuditLog.findAll", query = "SELECT c FROM CqlAuditLog c"),
    @NamedQuery(name = "CqlAuditLog.findById", query = "SELECT c FROM CqlAuditLog c WHERE c.id = :id"),
    @NamedQuery(name = "CqlAuditLog.findByCqlId", query = "SELECT c FROM CqlAuditLog c WHERE c.cqlId = :cqlId"),
    @NamedQuery(name = "CqlAuditLog.findByActivityType", query = "SELECT c FROM CqlAuditLog c WHERE c.activityType = :activityType"),
    @NamedQuery(name = "CqlAuditLog.findByUserId", query = "SELECT c FROM CqlAuditLog c WHERE c.userId = :userId"),
    @NamedQuery(name = "CqlAuditLog.findByTimestamp", query = "SELECT c FROM CqlAuditLog c WHERE c.timestamp = :timestamp"),
    @NamedQuery(name = "CqlAuditLog.findByAddlInfo", query = "SELECT c FROM CqlAuditLog c WHERE c.addlInfo = :addlInfo")})
public class CqlAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "CQL_ID")
    private String cqlId;
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

    public CqlAuditLog() {
    }

    public CqlAuditLog(String id) {
        this.id = id;
    }

    public CqlAuditLog(String id, String cqlId, String activityType, String userId, Date timestamp) {
        this.id = id;
        this.cqlId = cqlId;
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

    public String getCqlId() {
        return cqlId;
    }

    public void setCqlId(String cqlId) {
        this.cqlId = cqlId;
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
        if (!(object instanceof CqlAuditLog)) {
            return false;
        }
        CqlAuditLog other = (CqlAuditLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CqlAuditLog[ id=" + id + " ]";
    }
    
}
