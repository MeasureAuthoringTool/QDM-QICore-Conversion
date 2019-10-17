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
@Table(name = "DATABASECHANGELOGLOCK")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Databasechangeloglock.findAll", query = "SELECT d FROM Databasechangeloglock d"),
    @NamedQuery(name = "Databasechangeloglock.findById", query = "SELECT d FROM Databasechangeloglock d WHERE d.id = :id"),
    @NamedQuery(name = "Databasechangeloglock.findByLocked", query = "SELECT d FROM Databasechangeloglock d WHERE d.locked = :locked"),
    @NamedQuery(name = "Databasechangeloglock.findByLockgranted", query = "SELECT d FROM Databasechangeloglock d WHERE d.lockgranted = :lockgranted"),
    @NamedQuery(name = "Databasechangeloglock.findByLockedby", query = "SELECT d FROM Databasechangeloglock d WHERE d.lockedby = :lockedby")})
public class Databasechangeloglock implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "LOCKED")
    private boolean locked;
    @Column(name = "LOCKGRANTED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockgranted;
    @Column(name = "LOCKEDBY")
    private String lockedby;

    public Databasechangeloglock() {
    }

    public Databasechangeloglock(Integer id) {
        this.id = id;
    }

    public Databasechangeloglock(Integer id, boolean locked) {
        this.id = id;
        this.locked = locked;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean getLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Date getLockgranted() {
        return lockgranted;
    }

    public void setLockgranted(Date lockgranted) {
        this.lockgranted = lockgranted;
    }

    public String getLockedby() {
        return lockedby;
    }

    public void setLockedby(String lockedby) {
        this.lockedby = lockedby;
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
        if (!(object instanceof Databasechangeloglock)) {
            return false;
        }
        Databasechangeloglock other = (Databasechangeloglock) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Databasechangeloglock[ id=" + id + " ]";
    }
    
}
