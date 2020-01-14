package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "USER")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
    @NamedQuery(name = "User.findByUserId", query = "SELECT u FROM User u WHERE u.userId = :userId"),
    @NamedQuery(name = "User.findByFirstName", query = "SELECT u FROM User u WHERE u.firstName = :firstName"),
    @NamedQuery(name = "User.findByMiddleInitial", query = "SELECT u FROM User u WHERE u.middleInitial = :middleInitial"),
    @NamedQuery(name = "User.findByLastName", query = "SELECT u FROM User u WHERE u.lastName = :lastName"),
    @NamedQuery(name = "User.findByEmailAddress", query = "SELECT u FROM User u WHERE u.emailAddress = :emailAddress"),
    @NamedQuery(name = "User.findByPhoneNo", query = "SELECT u FROM User u WHERE u.phoneNo = :phoneNo"),
    @NamedQuery(name = "User.findByTitle", query = "SELECT u FROM User u WHERE u.title = :title"),
    @NamedQuery(name = "User.findByTerminationDate", query = "SELECT u FROM User u WHERE u.terminationDate = :terminationDate"),
    @NamedQuery(name = "User.findByActivationDate", query = "SELECT u FROM User u WHERE u.activationDate = :activationDate"),
    @NamedQuery(name = "User.findBySignInDate", query = "SELECT u FROM User u WHERE u.signInDate = :signInDate"),
    @NamedQuery(name = "User.findBySignOutDate", query = "SELECT u FROM User u WHERE u.signOutDate = :signOutDate"),
    @NamedQuery(name = "User.findByLockedOutDate", query = "SELECT u FROM User u WHERE u.lockedOutDate = :lockedOutDate"),
    @NamedQuery(name = "User.findByLoginId", query = "SELECT u FROM User u WHERE u.loginId = :loginId"),
    @NamedQuery(name = "User.findBySessionId", query = "SELECT u FROM User u WHERE u.sessionId = :sessionId")})
public class User implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Collection<UserAuditLog> userAuditLogCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Collection<RecentMsrActivityLog> recentMsrActivityLogCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureOwnerUserId")
    private Collection<MeasureShare> measureShareCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shareUserId")
    private Collection<MeasureShare> measureShareCollection1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Collection<UserPasswordHistory> userPasswordHistoryCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cqlLibraryOwnerUserId")
    private Collection<CqlLibraryShare> cqlLibraryShareCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shareUserId")
    private Collection<CqlLibraryShare> cqlLibraryShareCollection1;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ownerId")
    private Collection<CqlLibrary> cqlLibraryCollection;
    @OneToMany(mappedBy = "lockedUser")
    private Collection<CqlLibrary> cqlLibraryCollection1;
    @OneToMany(mappedBy = "lastModifiedBy")
    private Collection<CqlLibrary> cqlLibraryCollection2;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "userId")
    private UserPassword userPassword;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Collection<RecentCqlActivityLog> recentCqlActivityLogCollection;
    @OneToMany(mappedBy = "objectOwner")
    private Collection<ListObject> listObjectCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Collection<UserSecurityQuestions> userSecurityQuestionsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Collection<UserPreference> userPreferenceCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Collection<UserBonnieAccessInfo> userBonnieAccessInfoCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lastModifiedBy")
    private Collection<CqlLibraryHistory> cqlLibraryHistoryCollection;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "USER_ID")
    private String userId;
    @Basic(optional = false)
    @Column(name = "FIRST_NAME")
    private String firstName;
    @Column(name = "MIDDLE_INITIAL")
    private String middleInitial;
    @Basic(optional = false)
    @Column(name = "LAST_NAME")
    private String lastName;
    @Basic(optional = false)
    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress;
    @Basic(optional = false)
    @Column(name = "PHONE_NO")
    private String phoneNo;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "TERMINATION_DATE")
    @Temporal(TemporalType.DATE)
    private Date terminationDate;
    @Basic(optional = false)
    @Column(name = "ACTIVATION_DATE")
    @Temporal(TemporalType.DATE)
    private Date activationDate;
    @Column(name = "SIGN_IN_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date signInDate;
    @Column(name = "SIGN_OUT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date signOutDate;
    @Column(name = "LOCKED_OUT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockedOutDate;
    @Column(name = "LOGIN_ID")
    private String loginId;
    @Column(name = "SESSION_ID")
    private String sessionId;
    @OneToMany(mappedBy = "lockedUserId")
    private Collection<Measure> measureCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureOwnerId")
    private Collection<Measure> measureCollection1;
    @OneToMany(mappedBy = "lastModifiedBy")
    private Collection<Measure> measureCollection2;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "createUser")
    private Collection<AuditLog> auditLogCollection;
    @OneToMany(mappedBy = "updateUser")
    private Collection<AuditLog> auditLogCollection1;
    @JoinColumn(name = "ORG_ID", referencedColumnName = "ORG_ID")
    @ManyToOne(optional = false)
    private Organization orgId;
    @JoinColumn(name = "AUDIT_ID", referencedColumnName = "AUDIT_LOG_ID")
    @ManyToOne(optional = false)
    private AuditLog auditId;
    @JoinColumn(name = "SECURITY_ROLE_ID", referencedColumnName = "SECURITY_ROLE_ID")
    @ManyToOne(optional = false)
    private SecurityRole securityRoleId;
    @JoinColumn(name = "STATUS_ID", referencedColumnName = "STATUS_ID")
    @ManyToOne(optional = false)
    private Status statusId;

    public User() {
    }

    public User(String userId) {
        this.userId = userId;
    }

    public User(String userId, String firstName, String lastName, String emailAddress, String phoneNo, Date activationDate) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.phoneNo = phoneNo;
        this.activationDate = activationDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(Date terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Date getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    public Date getSignInDate() {
        return signInDate;
    }

    public void setSignInDate(Date signInDate) {
        this.signInDate = signInDate;
    }

    public Date getSignOutDate() {
        return signOutDate;
    }

    public void setSignOutDate(Date signOutDate) {
        this.signOutDate = signOutDate;
    }

    public Date getLockedOutDate() {
        return lockedOutDate;
    }

    public void setLockedOutDate(Date lockedOutDate) {
        this.lockedOutDate = lockedOutDate;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @XmlTransient
    public Collection<Measure> getMeasureCollection() {
        return measureCollection;
    }

    public void setMeasureCollection(Collection<Measure> measureCollection) {
        this.measureCollection = measureCollection;
    }

    @XmlTransient
    public Collection<Measure> getMeasureCollection1() {
        return measureCollection1;
    }

    public void setMeasureCollection1(Collection<Measure> measureCollection1) {
        this.measureCollection1 = measureCollection1;
    }

    @XmlTransient
    public Collection<Measure> getMeasureCollection2() {
        return measureCollection2;
    }

    public void setMeasureCollection2(Collection<Measure> measureCollection2) {
        this.measureCollection2 = measureCollection2;
    }

    @XmlTransient
    public Collection<AuditLog> getAuditLogCollection() {
        return auditLogCollection;
    }

    public void setAuditLogCollection(Collection<AuditLog> auditLogCollection) {
        this.auditLogCollection = auditLogCollection;
    }

    @XmlTransient
    public Collection<AuditLog> getAuditLogCollection1() {
        return auditLogCollection1;
    }

    public void setAuditLogCollection1(Collection<AuditLog> auditLogCollection1) {
        this.auditLogCollection1 = auditLogCollection1;
    }

    public Organization getOrgId() {
        return orgId;
    }

    public void setOrgId(Organization orgId) {
        this.orgId = orgId;
    }

    public AuditLog getAuditId() {
        return auditId;
    }

    public void setAuditId(AuditLog auditId) {
        this.auditId = auditId;
    }

    public SecurityRole getSecurityRoleId() {
        return securityRoleId;
    }

    public void setSecurityRoleId(SecurityRole securityRoleId) {
        this.securityRoleId = securityRoleId;
    }

    public Status getStatusId() {
        return statusId;
    }

    public void setStatusId(Status statusId) {
        this.statusId = statusId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.qdm.qicore.commons.model.User[ userId=" + userId + " ]";
    }

    @XmlTransient
    public Collection<UserAuditLog> getUserAuditLogCollection() {
        return userAuditLogCollection;
    }

    public void setUserAuditLogCollection(Collection<UserAuditLog> userAuditLogCollection) {
        this.userAuditLogCollection = userAuditLogCollection;
    }

    @XmlTransient
    public Collection<RecentMsrActivityLog> getRecentMsrActivityLogCollection() {
        return recentMsrActivityLogCollection;
    }

    public void setRecentMsrActivityLogCollection(Collection<RecentMsrActivityLog> recentMsrActivityLogCollection) {
        this.recentMsrActivityLogCollection = recentMsrActivityLogCollection;
    }

    @XmlTransient
    public Collection<MeasureShare> getMeasureShareCollection() {
        return measureShareCollection;
    }

    public void setMeasureShareCollection(Collection<MeasureShare> measureShareCollection) {
        this.measureShareCollection = measureShareCollection;
    }

    @XmlTransient
    public Collection<MeasureShare> getMeasureShareCollection1() {
        return measureShareCollection1;
    }

    public void setMeasureShareCollection1(Collection<MeasureShare> measureShareCollection1) {
        this.measureShareCollection1 = measureShareCollection1;
    }

    @XmlTransient
    public Collection<UserPasswordHistory> getUserPasswordHistoryCollection() {
        return userPasswordHistoryCollection;
    }

    public void setUserPasswordHistoryCollection(Collection<UserPasswordHistory> userPasswordHistoryCollection) {
        this.userPasswordHistoryCollection = userPasswordHistoryCollection;
    }

    @XmlTransient
    public Collection<CqlLibraryShare> getCqlLibraryShareCollection() {
        return cqlLibraryShareCollection;
    }

    public void setCqlLibraryShareCollection(Collection<CqlLibraryShare> cqlLibraryShareCollection) {
        this.cqlLibraryShareCollection = cqlLibraryShareCollection;
    }

    @XmlTransient
    public Collection<CqlLibraryShare> getCqlLibraryShareCollection1() {
        return cqlLibraryShareCollection1;
    }

    public void setCqlLibraryShareCollection1(Collection<CqlLibraryShare> cqlLibraryShareCollection1) {
        this.cqlLibraryShareCollection1 = cqlLibraryShareCollection1;
    }

    @XmlTransient
    public Collection<CqlLibrary> getCqlLibraryCollection() {
        return cqlLibraryCollection;
    }

    public void setCqlLibraryCollection(Collection<CqlLibrary> cqlLibraryCollection) {
        this.cqlLibraryCollection = cqlLibraryCollection;
    }

    @XmlTransient
    public Collection<CqlLibrary> getCqlLibraryCollection1() {
        return cqlLibraryCollection1;
    }

    public void setCqlLibraryCollection1(Collection<CqlLibrary> cqlLibraryCollection1) {
        this.cqlLibraryCollection1 = cqlLibraryCollection1;
    }

    @XmlTransient
    public Collection<CqlLibrary> getCqlLibraryCollection2() {
        return cqlLibraryCollection2;
    }

    public void setCqlLibraryCollection2(Collection<CqlLibrary> cqlLibraryCollection2) {
        this.cqlLibraryCollection2 = cqlLibraryCollection2;
    }

    public UserPassword getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(UserPassword userPassword) {
        this.userPassword = userPassword;
    }

    @XmlTransient
    public Collection<RecentCqlActivityLog> getRecentCqlActivityLogCollection() {
        return recentCqlActivityLogCollection;
    }

    public void setRecentCqlActivityLogCollection(Collection<RecentCqlActivityLog> recentCqlActivityLogCollection) {
        this.recentCqlActivityLogCollection = recentCqlActivityLogCollection;
    }

    @XmlTransient
    public Collection<ListObject> getListObjectCollection() {
        return listObjectCollection;
    }

    public void setListObjectCollection(Collection<ListObject> listObjectCollection) {
        this.listObjectCollection = listObjectCollection;
    }

    @XmlTransient
    public Collection<UserSecurityQuestions> getUserSecurityQuestionsCollection() {
        return userSecurityQuestionsCollection;
    }

    public void setUserSecurityQuestionsCollection(Collection<UserSecurityQuestions> userSecurityQuestionsCollection) {
        this.userSecurityQuestionsCollection = userSecurityQuestionsCollection;
    }

    @XmlTransient
    public Collection<UserPreference> getUserPreferenceCollection() {
        return userPreferenceCollection;
    }

    public void setUserPreferenceCollection(Collection<UserPreference> userPreferenceCollection) {
        this.userPreferenceCollection = userPreferenceCollection;
    }

    @XmlTransient
    public Collection<UserBonnieAccessInfo> getUserBonnieAccessInfoCollection() {
        return userBonnieAccessInfoCollection;
    }

    public void setUserBonnieAccessInfoCollection(Collection<UserBonnieAccessInfo> userBonnieAccessInfoCollection) {
        this.userBonnieAccessInfoCollection = userBonnieAccessInfoCollection;
    }

    @XmlTransient
    public Collection<CqlLibraryHistory> getCqlLibraryHistoryCollection() {
        return cqlLibraryHistoryCollection;
    }

    public void setCqlLibraryHistoryCollection(Collection<CqlLibraryHistory> cqlLibraryHistoryCollection) {
        this.cqlLibraryHistoryCollection = cqlLibraryHistoryCollection;
    }
    
}
