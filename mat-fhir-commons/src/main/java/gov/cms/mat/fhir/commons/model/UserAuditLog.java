/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "USER_AUDIT_LOG")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserAuditLog.findAll", query = "SELECT u FROM UserAuditLog u"),
    @NamedQuery(name = "UserAuditLog.findById", query = "SELECT u FROM UserAuditLog u WHERE u.id = :id"),
    @NamedQuery(name = "UserAuditLog.findByActionType", query = "SELECT u FROM UserAuditLog u WHERE u.actionType = :actionType"),
    @NamedQuery(name = "UserAuditLog.findByActivityType", query = "SELECT u FROM UserAuditLog u WHERE u.activityType = :activityType"),
    @NamedQuery(name = "UserAuditLog.findByUserEmail", query = "SELECT u FROM UserAuditLog u WHERE u.userEmail = :userEmail"),
    @NamedQuery(name = "UserAuditLog.findByTimestamp", query = "SELECT u FROM UserAuditLog u WHERE u.timestamp = :timestamp"),
    @NamedQuery(name = "UserAuditLog.findByAddlInfo", query = "SELECT u FROM UserAuditLog u WHERE u.addlInfo = :addlInfo")})
public class UserAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "ACTION_TYPE")
    private String actionType;
    @Basic(optional = false)
    @Column(name = "ACTIVITY_TYPE")
    private String activityType;
    @Basic(optional = false)
    @Column(name = "USER_EMAIL")
    private String userEmail;
    @Basic(optional = false)
    @Column(name = "TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    @Column(name = "ADDL_INFO")
    private String addlInfo;
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User userId;

    public UserAuditLog() {
    }

    public UserAuditLog(String id) {
        this.id = id;
    }

    public UserAuditLog(String id, String actionType, String activityType, String userEmail, Date timestamp) {
        this.id = id;
        this.actionType = actionType;
        this.activityType = activityType;
        this.userEmail = userEmail;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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
        if (!(object instanceof UserAuditLog)) {
            return false;
        }
        UserAuditLog other = (UserAuditLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.UserAuditLog[ id=" + id + " ]";
    }
    
}
