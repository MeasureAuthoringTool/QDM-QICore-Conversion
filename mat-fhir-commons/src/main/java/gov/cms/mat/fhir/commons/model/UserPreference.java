package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "USER_PREFERENCE")

@NamedQueries({
        @NamedQuery(name = "UserPreference.findAll", query = "SELECT u FROM UserPreference u"),
        @NamedQuery(name = "UserPreference.findById", query = "SELECT u FROM UserPreference u WHERE u.id = :id"),
        @NamedQuery(name = "UserPreference.findByFreeTextEditorEnabled", query = "SELECT u FROM UserPreference u WHERE u.freeTextEditorEnabled = :freeTextEditorEnabled")})
public class UserPreference implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "FREE_TEXT_EDITOR_ENABLED")
    private Boolean freeTextEditorEnabled;
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User userId;

    public UserPreference() {
    }

    public UserPreference(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getFreeTextEditorEnabled() {
        return freeTextEditorEnabled;
    }

    public void setFreeTextEditorEnabled(Boolean freeTextEditorEnabled) {
        this.freeTextEditorEnabled = freeTextEditorEnabled;
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
        if (!(object instanceof UserPreference)) {
            return false;
        }
        UserPreference other = (UserPreference) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.UserPreference[ id=" + id + " ]";
    }

}
