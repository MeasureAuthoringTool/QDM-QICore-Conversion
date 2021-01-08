package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;


import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "MEASURE_SET")

@NamedQueries({
        @NamedQuery(name = "MeasureSet.findAll", query = "SELECT m FROM MeasureSet m"),
        @NamedQuery(name = "MeasureSet.findById", query = "SELECT m FROM MeasureSet m WHERE m.id = :id"),
        @NamedQuery(name = "MeasureSet.findByName", query = "SELECT m FROM MeasureSet m WHERE m.name = :name")})
public class MeasureSet implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "NAME")
    private String name;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "measureSetId")
    private Collection<Measure> measureCollection;

    public MeasureSet() {
    }

    public MeasureSet(String id) {
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


    public Collection<Measure> getMeasureCollection() {
        return measureCollection;
    }

    public void setMeasureCollection(Collection<Measure> measureCollection) {
        this.measureCollection = measureCollection;
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
        if (!(object instanceof MeasureSet)) {
            return false;
        }
        MeasureSet other = (MeasureSet) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.qdm.qicore.commons.model.MeasureSet[ id=" + id + " ]";
    }

}
