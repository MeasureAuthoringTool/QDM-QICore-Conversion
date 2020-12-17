package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;


import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "MEASURE_TYPES")

@NamedQueries({
        @NamedQuery(name = "MeasureTypes.findAll", query = "SELECT m FROM MeasureTypes m"),
        @NamedQuery(name = "MeasureTypes.findById", query = "SELECT m FROM MeasureTypes m WHERE m.id = :id"),
        @NamedQuery(name = "MeasureTypes.findByName", query = "SELECT m FROM MeasureTypes m WHERE m.name = :name"),
        @NamedQuery(name = "MeasureTypes.findByAbbrName", query = "SELECT m FROM MeasureTypes m WHERE m.abbrName = :abbrName")})
public class MeasureTypes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "NAME")
    private String name;
    @Column(name = "ABBR_NAME")
    private String abbrName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureTypeId")
    private Collection<MeasureTypeAssociation> measureTypeAssociationCollection;

    public MeasureTypes() {
    }

    public MeasureTypes(String id) {
        this.id = id;
    }

    public MeasureTypes(String id, String name) {
        this.id = id;
        this.name = name;
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

    public String getAbbrName() {
        return abbrName;
    }

    public void setAbbrName(String abbrName) {
        this.abbrName = abbrName;
    }


    public Collection<MeasureTypeAssociation> getMeasureTypeAssociationCollection() {
        return measureTypeAssociationCollection;
    }

    public void setMeasureTypeAssociationCollection(Collection<MeasureTypeAssociation> measureTypeAssociationCollection) {
        this.measureTypeAssociationCollection = measureTypeAssociationCollection;
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
        if (!(object instanceof MeasureTypes)) {
            return false;
        }
        MeasureTypes other = (MeasureTypes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureTypes[ id=" + id + " ]";
    }

}
