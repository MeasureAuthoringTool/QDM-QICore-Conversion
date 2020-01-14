package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "MEASURE_VALIDATION_LOG")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MeasureValidationLog.findAll", query = "SELECT m FROM MeasureValidationLog m"),
    @NamedQuery(name = "MeasureValidationLog.findById", query = "SELECT m FROM MeasureValidationLog m WHERE m.id = :id"),
    @NamedQuery(name = "MeasureValidationLog.findByMeasureId", query = "SELECT m FROM MeasureValidationLog m WHERE m.measureId = :measureId"),
    @NamedQuery(name = "MeasureValidationLog.findByActivityType", query = "SELECT m FROM MeasureValidationLog m WHERE m.activityType = :activityType"),
    @NamedQuery(name = "MeasureValidationLog.findByUserId", query = "SELECT m FROM MeasureValidationLog m WHERE m.userId = :userId"),
    @NamedQuery(name = "MeasureValidationLog.findByTimestamp", query = "SELECT m FROM MeasureValidationLog m WHERE m.timestamp = :timestamp")})
public class MeasureValidationLog implements Serializable {

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
    @Lob
    @Column(name = "INTERIM_BLOB")
    private byte[] interimBlob;

    public MeasureValidationLog() {
    }

    public MeasureValidationLog(String id) {
        this.id = id;
    }

    public MeasureValidationLog(String id, String measureId, String activityType, String userId, Date timestamp) {
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

    public byte[] getInterimBlob() {
        return interimBlob;
    }

    public void setInterimBlob(byte[] interimBlob) {
        this.interimBlob = interimBlob;
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
        if (!(object instanceof MeasureValidationLog)) {
            return false;
        }
        MeasureValidationLog other = (MeasureValidationLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureValidationLog[ id=" + id + " ]";
    }
    
}
