package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;


import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "MODES")

@NamedQueries({
        @NamedQuery(name = "Modes.findAll", query = "SELECT m FROM Modes m"),
        @NamedQuery(name = "Modes.findById", query = "SELECT m FROM Modes m WHERE m.id = :id"),
        @NamedQuery(name = "Modes.findByModeName", query = "SELECT m FROM Modes m WHERE m.modeName = :modeName")})
public class Modes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "MODE_NAME")
    private String modeName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "modeId")
    private Collection<AttributesModes> attributesModesCollection;

    public Modes() {
    }

    public Modes(Integer id) {
        this.id = id;
    }

    public Modes(Integer id, String modeName) {
        this.id = id;
        this.modeName = modeName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }


    public Collection<AttributesModes> getAttributesModesCollection() {
        return attributesModesCollection;
    }

    public void setAttributesModesCollection(Collection<AttributesModes> attributesModesCollection) {
        this.attributesModesCollection = attributesModesCollection;
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
        if (!(object instanceof Modes)) {
            return false;
        }
        Modes other = (Modes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Modes[ id=" + id + " ]";
    }

}
