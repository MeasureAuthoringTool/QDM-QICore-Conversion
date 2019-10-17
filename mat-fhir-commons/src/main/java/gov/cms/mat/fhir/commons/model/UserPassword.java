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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "USER_PASSWORD")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserPassword.findAll", query = "SELECT u FROM UserPassword u"),
    @NamedQuery(name = "UserPassword.findByUserPasswordId", query = "SELECT u FROM UserPassword u WHERE u.userPasswordId = :userPasswordId"),
    @NamedQuery(name = "UserPassword.findByPwdLockCounter", query = "SELECT u FROM UserPassword u WHERE u.pwdLockCounter = :pwdLockCounter"),
    @NamedQuery(name = "UserPassword.findByForgotPwdLockCounter", query = "SELECT u FROM UserPassword u WHERE u.forgotPwdLockCounter = :forgotPwdLockCounter"),
    @NamedQuery(name = "UserPassword.findByPassword", query = "SELECT u FROM UserPassword u WHERE u.password = :password"),
    @NamedQuery(name = "UserPassword.findBySalt", query = "SELECT u FROM UserPassword u WHERE u.salt = :salt"),
    @NamedQuery(name = "UserPassword.findByInitialPwd", query = "SELECT u FROM UserPassword u WHERE u.initialPwd = :initialPwd"),
    @NamedQuery(name = "UserPassword.findByCreateDate", query = "SELECT u FROM UserPassword u WHERE u.createDate = :createDate"),
    @NamedQuery(name = "UserPassword.findByFirstFailedAttemptTime", query = "SELECT u FROM UserPassword u WHERE u.firstFailedAttemptTime = :firstFailedAttemptTime"),
    @NamedQuery(name = "UserPassword.findByTempPwd", query = "SELECT u FROM UserPassword u WHERE u.tempPwd = :tempPwd")})
public class UserPassword implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "USER_PASSWORD_ID")
    private String userPasswordId;
    @Column(name = "PWD_LOCK_COUNTER")
    private Integer pwdLockCounter;
    @Column(name = "FORGOT_PWD_LOCK_COUNTER")
    private Integer forgotPwdLockCounter;
    @Basic(optional = false)
    @Column(name = "PASSWORD")
    private String password;
    @Basic(optional = false)
    @Column(name = "SALT")
    private String salt;
    @Column(name = "INITIAL_PWD")
    private Boolean initialPwd;
    @Basic(optional = false)
    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.DATE)
    private Date createDate;
    @Column(name = "FIRST_FAILED_ATTEMPT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date firstFailedAttemptTime;
    @Column(name = "TEMP_PWD")
    private Boolean tempPwd;
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    @OneToOne(optional = false)
    private User userId;

    public UserPassword() {
    }

    public UserPassword(String userPasswordId) {
        this.userPasswordId = userPasswordId;
    }

    public UserPassword(String userPasswordId, String password, String salt, Date createDate) {
        this.userPasswordId = userPasswordId;
        this.password = password;
        this.salt = salt;
        this.createDate = createDate;
    }

    public String getUserPasswordId() {
        return userPasswordId;
    }

    public void setUserPasswordId(String userPasswordId) {
        this.userPasswordId = userPasswordId;
    }

    public Integer getPwdLockCounter() {
        return pwdLockCounter;
    }

    public void setPwdLockCounter(Integer pwdLockCounter) {
        this.pwdLockCounter = pwdLockCounter;
    }

    public Integer getForgotPwdLockCounter() {
        return forgotPwdLockCounter;
    }

    public void setForgotPwdLockCounter(Integer forgotPwdLockCounter) {
        this.forgotPwdLockCounter = forgotPwdLockCounter;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Boolean getInitialPwd() {
        return initialPwd;
    }

    public void setInitialPwd(Boolean initialPwd) {
        this.initialPwd = initialPwd;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getFirstFailedAttemptTime() {
        return firstFailedAttemptTime;
    }

    public void setFirstFailedAttemptTime(Date firstFailedAttemptTime) {
        this.firstFailedAttemptTime = firstFailedAttemptTime;
    }

    public Boolean getTempPwd() {
        return tempPwd;
    }

    public void setTempPwd(Boolean tempPwd) {
        this.tempPwd = tempPwd;
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
        hash += (userPasswordId != null ? userPasswordId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserPassword)) {
            return false;
        }
        UserPassword other = (UserPassword) object;
        if ((this.userPasswordId == null && other.userPasswordId != null) || (this.userPasswordId != null && !this.userPasswordId.equals(other.userPasswordId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.UserPassword[ userPasswordId=" + userPasswordId + " ]";
    }
    
}
