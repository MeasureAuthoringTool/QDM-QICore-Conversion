package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "EMAIL_AUDIT_LOG")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "EmailAuditLog.findAll", query = "SELECT e FROM EmailAuditLog e"),
    @NamedQuery(name = "EmailAuditLog.findById", query = "SELECT e FROM EmailAuditLog e WHERE e.id = :id"),
    @NamedQuery(name = "EmailAuditLog.findByLoginId", query = "SELECT e FROM EmailAuditLog e WHERE e.loginId = :loginId"),
    @NamedQuery(name = "EmailAuditLog.findByTimestamp", query = "SELECT e FROM EmailAuditLog e WHERE e.timestamp = :timestamp"),
    @NamedQuery(name = "EmailAuditLog.findByActivityType", query = "SELECT e FROM EmailAuditLog e WHERE e.activityType = :activityType")})
public class EmailAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "LOGIN_ID")
    private String loginId;
    @Basic(optional = false)
    @Column(name = "TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    @Column(name = "ACTIVITY_TYPE")
    private String activityType;

    public EmailAuditLog() {
    }

    public EmailAuditLog(String id) {
        this.id = id;
    }

    public EmailAuditLog(String id, String loginId, Date timestamp) {
        this.id = id;
        this.loginId = loginId;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
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
        if (!(object instanceof EmailAuditLog)) {
            return false;
        }
        EmailAuditLog other = (EmailAuditLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.EmailAuditLog[ id=" + id + " ]";
    }
    
}
