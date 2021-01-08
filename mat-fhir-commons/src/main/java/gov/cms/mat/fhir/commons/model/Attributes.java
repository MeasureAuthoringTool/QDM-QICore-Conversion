package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ATTRIBUTES")
@NamedQueries({
        @NamedQuery(name = "Attributes.findAll", query = "SELECT a FROM Attributes a"),
        @NamedQuery(name = "Attributes.findById", query = "SELECT a FROM Attributes a WHERE a.id = :id"),
        @NamedQuery(name = "Attributes.findByAttributeName", query = "SELECT a FROM Attributes a WHERE a.attributeName = :attributeName")})
public class Attributes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "ATTRIBUTE_NAME")
    private String attributeName;

    public Attributes() {
    }

    public Attributes(Integer id) {
        this.id = id;
    }

    public Attributes(Integer id, String attributeName) {
        this.id = id;
        this.attributeName = attributeName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
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
        if (!(object instanceof Attributes)) {
            return false;
        }
        Attributes other = (Attributes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Attributes[ id=" + id + " ]";
    }

}
