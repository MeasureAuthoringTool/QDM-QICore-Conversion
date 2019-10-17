/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "UNIT_TYPE_MATRIX")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UnitTypeMatrix.findAll", query = "SELECT u FROM UnitTypeMatrix u"),
    @NamedQuery(name = "UnitTypeMatrix.findById", query = "SELECT u FROM UnitTypeMatrix u WHERE u.id = :id")})
public class UnitTypeMatrix implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @JoinColumn(name = "FK_UNIT_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Unit fkUnitId;
    @JoinColumn(name = "FK_UNIT_TYPE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private UnitType fkUnitTypeId;

    public UnitTypeMatrix() {
    }

    public UnitTypeMatrix(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Unit getFkUnitId() {
        return fkUnitId;
    }

    public void setFkUnitId(Unit fkUnitId) {
        this.fkUnitId = fkUnitId;
    }

    public UnitType getFkUnitTypeId() {
        return fkUnitTypeId;
    }

    public void setFkUnitTypeId(UnitType fkUnitTypeId) {
        this.fkUnitTypeId = fkUnitTypeId;
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
        if (!(object instanceof UnitTypeMatrix)) {
            return false;
        }
        UnitTypeMatrix other = (UnitTypeMatrix) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.UnitTypeMatrix[ id=" + id + " ]";
    }
    
}
