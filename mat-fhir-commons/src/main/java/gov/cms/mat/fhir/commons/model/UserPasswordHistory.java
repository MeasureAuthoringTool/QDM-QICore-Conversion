package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "USER_PASSWORD_HISTORY")

@NamedQueries({
        @NamedQuery(name = "UserPasswordHistory.findAll", query = "SELECT u FROM UserPasswordHistory u"),
        @NamedQuery(name = "UserPasswordHistory.findByUserPasswordHistoryId", query = "SELECT u FROM UserPasswordHistory u WHERE u.userPasswordHistoryId = :userPasswordHistoryId"),
        @NamedQuery(name = "UserPasswordHistory.findByPassword", query = "SELECT u FROM UserPasswordHistory u WHERE u.password = :password"),
        @NamedQuery(name = "UserPasswordHistory.findBySalt", query = "SELECT u FROM UserPasswordHistory u WHERE u.salt = :salt"),
        @NamedQuery(name = "UserPasswordHistory.findByCreateDate", query = "SELECT u FROM UserPasswordHistory u WHERE u.createDate = :createDate")})
public class UserPasswordHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "USER_PASSWORD_HISTORY_ID")
    private String userPasswordHistoryId;
    @Basic(optional = false)
    @Column(name = "PASSWORD")
    private String password;
    @Basic(optional = false)
    @Column(name = "SALT")
    private String salt;
    @Basic(optional = false)
    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.DATE)
    private Date createDate;
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User userId;

    public UserPasswordHistory() {
    }

    public UserPasswordHistory(String userPasswordHistoryId) {
        this.userPasswordHistoryId = userPasswordHistoryId;
    }

    public UserPasswordHistory(String userPasswordHistoryId, String password, String salt, Date createDate) {
        this.userPasswordHistoryId = userPasswordHistoryId;
        this.password = password;
        this.salt = salt;
        this.createDate = createDate;
    }

    public String getUserPasswordHistoryId() {
        return userPasswordHistoryId;
    }

    public void setUserPasswordHistoryId(String userPasswordHistoryId) {
        this.userPasswordHistoryId = userPasswordHistoryId;
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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
        hash += (userPasswordHistoryId != null ? userPasswordHistoryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserPasswordHistory)) {
            return false;
        }
        UserPasswordHistory other = (UserPasswordHistory) object;
        if ((this.userPasswordHistoryId == null && other.userPasswordHistoryId != null) || (this.userPasswordHistoryId != null && !this.userPasswordHistoryId.equals(other.userPasswordHistoryId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.UserPasswordHistory[ userPasswordHistoryId=" + userPasswordHistoryId + " ]";
    }

}
