/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "SECURITY_ROLE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SecurityRole.findAll", query = "SELECT s FROM SecurityRole s"),
    @NamedQuery(name = "SecurityRole.findBySecurityRoleId", query = "SELECT s FROM SecurityRole s WHERE s.securityRoleId = :securityRoleId"),
    @NamedQuery(name = "SecurityRole.findByDescription", query = "SELECT s FROM SecurityRole s WHERE s.description = :description")})
public class SecurityRole implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "SECURITY_ROLE_ID")
    private String securityRoleId;
    @Basic(optional = false)
    @Column(name = "DESCRIPTION")
    private String description;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "securityRoleId")
    private Collection<User> userCollection;

    public SecurityRole() {
    }

    public SecurityRole(String securityRoleId) {
        this.securityRoleId = securityRoleId;
    }

    public SecurityRole(String securityRoleId, String description) {
        this.securityRoleId = securityRoleId;
        this.description = description;
    }

    public String getSecurityRoleId() {
        return securityRoleId;
    }

    public void setSecurityRoleId(String securityRoleId) {
        this.securityRoleId = securityRoleId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    public Collection<User> getUserCollection() {
        return userCollection;
    }

    public void setUserCollection(Collection<User> userCollection) {
        this.userCollection = userCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (securityRoleId != null ? securityRoleId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SecurityRole)) {
            return false;
        }
        SecurityRole other = (SecurityRole) object;
        if ((this.securityRoleId == null && other.securityRoleId != null) || (this.securityRoleId != null && !this.securityRoleId.equals(other.securityRoleId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.qdm.qicore.commons.model.SecurityRole[ securityRoleId=" + securityRoleId + " ]";
    }
    
}
