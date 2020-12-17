package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "MEASURE_AUDIT_LOG")

@NamedQueries({
        @NamedQuery(name = "MeasureAuditLog.findAll", query = "SELECT m FROM MeasureAuditLog m"),
        @NamedQuery(name = "MeasureAuditLog.findById", query = "SELECT m FROM MeasureAuditLog m WHERE m.id = :id"),
        @NamedQuery(name = "MeasureAuditLog.findByMeasureId", query = "SELECT m FROM MeasureAuditLog m WHERE m.measureId = :measureId"),
        @NamedQuery(name = "MeasureAuditLog.findByActivityType", query = "SELECT m FROM MeasureAuditLog m WHERE m.activityType = :activityType"),
        @NamedQuery(name = "MeasureAuditLog.findByUserId", query = "SELECT m FROM MeasureAuditLog m WHERE m.userId = :userId"),
        @NamedQuery(name = "MeasureAuditLog.findByTimestamp", query = "SELECT m FROM MeasureAuditLog m WHERE m.timestamp = :timestamp"),
        @NamedQuery(name = "MeasureAuditLog.findByAddlInfo", query = "SELECT m FROM MeasureAuditLog m WHERE m.addlInfo = :addlInfo")})
public class MeasureAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "MEASURE_ID")
    private String measureId;
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

    public MeasureAuditLog() {
    }

    public MeasureAuditLog(String id) {
        this.id = id;
    }

    public MeasureAuditLog(String id, String measureId, String activityType, String userId, Date timestamp) {
        this.id = id;
        this.measureId = measureId;
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

    public String getMeasureId() {
        return measureId;
    }

    public void setMeasureId(String measureId) {
        this.measureId = measureId;
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
        if (!(object instanceof MeasureAuditLog)) {
            return false;
        }
        MeasureAuditLog other = (MeasureAuditLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureAuditLog[ id=" + id + " ]";
    }

}
