package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "CODE_LIST_AUDIT_LOG")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CodeListAuditLog.findAll", query = "SELECT c FROM CodeListAuditLog c"),
    @NamedQuery(name = "CodeListAuditLog.findById", query = "SELECT c FROM CodeListAuditLog c WHERE c.id = :id"),
    @NamedQuery(name = "CodeListAuditLog.findByCodeListId", query = "SELECT c FROM CodeListAuditLog c WHERE c.codeListId = :codeListId"),
    @NamedQuery(name = "CodeListAuditLog.findByActivityType", query = "SELECT c FROM CodeListAuditLog c WHERE c.activityType = :activityType"),
    @NamedQuery(name = "CodeListAuditLog.findByUserId", query = "SELECT c FROM CodeListAuditLog c WHERE c.userId = :userId"),
    @NamedQuery(name = "CodeListAuditLog.findByTimestamp", query = "SELECT c FROM CodeListAuditLog c WHERE c.timestamp = :timestamp"),
    @NamedQuery(name = "CodeListAuditLog.findByAddlInfo", query = "SELECT c FROM CodeListAuditLog c WHERE c.addlInfo = :addlInfo")})
public class CodeListAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "CODE_LIST_ID")
    private String codeListId;
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

    public CodeListAuditLog() {
    }

    public CodeListAuditLog(String id) {
        this.id = id;
    }

    public CodeListAuditLog(String id, String codeListId, String activityType, String userId, Date timestamp) {
        this.id = id;
        this.codeListId = codeListId;
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

    public String getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(String codeListId) {
        this.codeListId = codeListId;
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
        if (!(object instanceof CodeListAuditLog)) {
            return false;
        }
        CodeListAuditLog other = (CodeListAuditLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CodeListAuditLog[ id=" + id + " ]";
    }
    
}
