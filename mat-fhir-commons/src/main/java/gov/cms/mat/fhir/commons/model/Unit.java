package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;


import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "UNIT")

@NamedQueries({
        @NamedQuery(name = "Unit.findAll", query = "SELECT u FROM Unit u"),
        @NamedQuery(name = "Unit.findById", query = "SELECT u FROM Unit u WHERE u.id = :id"),
        @NamedQuery(name = "Unit.findByName", query = "SELECT u FROM Unit u WHERE u.name = :name"),
        @NamedQuery(name = "Unit.findBySortOrder", query = "SELECT u FROM Unit u WHERE u.sortOrder = :sortOrder"),
        @NamedQuery(name = "Unit.findByCqlUnit", query = "SELECT u FROM Unit u WHERE u.cqlUnit = :cqlUnit")})
public class Unit implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "NAME")
    private String name;
    @Basic(optional = false)
    @Column(name = "SORT_ORDER")
    private int sortOrder;
    @Column(name = "CQL_UNIT")
    private String cqlUnit;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkUnitId")
    private Collection<UnitTypeMatrix> unitTypeMatrixCollection;

    public Unit() {
    }

    public Unit(String id) {
        this.id = id;
    }

    public Unit(String id, int sortOrder) {
        this.id = id;
        this.sortOrder = sortOrder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getCqlUnit() {
        return cqlUnit;
    }

    public void setCqlUnit(String cqlUnit) {
        this.cqlUnit = cqlUnit;
    }


    public Collection<UnitTypeMatrix> getUnitTypeMatrixCollection() {
        return unitTypeMatrixCollection;
    }

    public void setUnitTypeMatrixCollection(Collection<UnitTypeMatrix> unitTypeMatrixCollection) {
        this.unitTypeMatrixCollection = unitTypeMatrixCollection;
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
        if (!(object instanceof Unit)) {
            return false;
        }
        Unit other = (Unit) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Unit[ id=" + id + " ]";
    }

}
