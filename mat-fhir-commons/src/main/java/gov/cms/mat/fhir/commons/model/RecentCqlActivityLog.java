package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "RECENT_CQL_ACTIVITY_LOG")

@NamedQueries({
        @NamedQuery(name = "RecentCqlActivityLog.findAll", query = "SELECT r FROM RecentCqlActivityLog r"),
        @NamedQuery(name = "RecentCqlActivityLog.findById", query = "SELECT r FROM RecentCqlActivityLog r WHERE r.id = :id"),
        @NamedQuery(name = "RecentCqlActivityLog.findByTimestamp", query = "SELECT r FROM RecentCqlActivityLog r WHERE r.timestamp = :timestamp")})
public class RecentCqlActivityLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    @JoinColumn(name = "CQL_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private CqlLibrary cqlId;
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User userId;

    public RecentCqlActivityLog() {
    }

    public RecentCqlActivityLog(String id) {
        this.id = id;
    }

    public RecentCqlActivityLog(String id, Date timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public CqlLibrary getCqlId() {
        return cqlId;
    }

    public void setCqlId(CqlLibrary cqlId) {
        this.cqlId = cqlId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
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
        if (!(object instanceof RecentCqlActivityLog)) {
            return false;
        }
        RecentCqlActivityLog other = (RecentCqlActivityLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.RecentCqlActivityLog[ id=" + id + " ]";
    }

}
