package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "UNIT_TYPE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UnitType.findAll", query = "SELECT u FROM UnitType u"),
    @NamedQuery(name = "UnitType.findById", query = "SELECT u FROM UnitType u WHERE u.id = :id"),
    @NamedQuery(name = "UnitType.findByName", query = "SELECT u FROM UnitType u WHERE u.name = :name")})
public class UnitType implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "NAME")
    private String name;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkUnitTypeId")
    private Collection<UnitTypeMatrix> unitTypeMatrixCollection;

    public UnitType() {
    }

    public UnitType(String id) {
        this.id = id;
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

    @XmlTransient
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
        if (!(object instanceof UnitType)) {
            return false;
        }
        UnitType other = (UnitType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.UnitType[ id=" + id + " ]";
    }
    
}
