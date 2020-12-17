package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "USER_BONNIE_ACCESS_INFO")

@NamedQueries({
        @NamedQuery(name = "UserBonnieAccessInfo.findAll", query = "SELECT u FROM UserBonnieAccessInfo u"),
        @NamedQuery(name = "UserBonnieAccessInfo.findById", query = "SELECT u FROM UserBonnieAccessInfo u WHERE u.id = :id"),
        @NamedQuery(name = "UserBonnieAccessInfo.findByRefreshToken", query = "SELECT u FROM UserBonnieAccessInfo u WHERE u.refreshToken = :refreshToken"),
        @NamedQuery(name = "UserBonnieAccessInfo.findByAccessToken", query = "SELECT u FROM UserBonnieAccessInfo u WHERE u.accessToken = :accessToken")})
public class UserBonnieAccessInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "REFRESH_TOKEN")
    private String refreshToken;
    @Basic(optional = false)
    @Column(name = "ACCESS_TOKEN")
    private String accessToken;
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User userId;

    public UserBonnieAccessInfo() {
    }

    public UserBonnieAccessInfo(Integer id) {
        this.id = id;
    }

    public UserBonnieAccessInfo(Integer id, String refreshToken, String accessToken) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
        if (!(object instanceof UserBonnieAccessInfo)) {
            return false;
        }
        UserBonnieAccessInfo other = (UserBonnieAccessInfo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.UserBonnieAccessInfo[ id=" + id + " ]";
    }

}
