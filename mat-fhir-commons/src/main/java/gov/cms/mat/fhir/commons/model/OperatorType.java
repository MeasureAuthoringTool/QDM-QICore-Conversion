package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;


import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "OPERATOR_TYPE")

@NamedQueries({
        @NamedQuery(name = "OperatorType.findAll", query = "SELECT o FROM OperatorType o"),
        @NamedQuery(name = "OperatorType.findById", query = "SELECT o FROM OperatorType o WHERE o.id = :id"),
        @NamedQuery(name = "OperatorType.findByName", query = "SELECT o FROM OperatorType o WHERE o.name = :name")})
public class OperatorType implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "NAME")
    private String name;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fkOperatorType")
    private Collection<Operator> operatorCollection;

    public OperatorType() {
    }

    public OperatorType(String id) {
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


    public Collection<Operator> getOperatorCollection() {
        return operatorCollection;
    }

    public void setOperatorCollection(Collection<Operator> operatorCollection) {
        this.operatorCollection = operatorCollection;
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
        if (!(object instanceof OperatorType)) {
            return false;
        }
        OperatorType other = (OperatorType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.OperatorType[ id=" + id + " ]";
    }

}
