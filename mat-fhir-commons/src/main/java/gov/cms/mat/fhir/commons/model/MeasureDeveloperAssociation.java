package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "MEASURE_DEVELOPER_ASSOCIATION")

@NamedQueries({
        @NamedQuery(name = "MeasureDeveloperAssociation.findAll", query = "SELECT m FROM MeasureDeveloperAssociation m"),
        @NamedQuery(name = "MeasureDeveloperAssociation.findById", query = "SELECT m FROM MeasureDeveloperAssociation m WHERE m.id = :id")})
public class MeasureDeveloperAssociation implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @JoinColumn(name = "MEASURE_DEVELOPER_ID", referencedColumnName = "ORG_ID")
    @ManyToOne
    private Organization measureDeveloperId;
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Measure measureId;

    public MeasureDeveloperAssociation() {
    }

    public MeasureDeveloperAssociation(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Organization getMeasureDeveloperId() {
        return measureDeveloperId;
    }

    public void setMeasureDeveloperId(Organization measureDeveloperId) {
        this.measureDeveloperId = measureDeveloperId;
    }

    public Measure getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Measure measureId) {
        this.measureId = measureId;
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
        if (!(object instanceof MeasureDeveloperAssociation)) {
            return false;
        }
        MeasureDeveloperAssociation other = (MeasureDeveloperAssociation) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureDeveloperAssociation[ id=" + id + " ]";
    }

}
