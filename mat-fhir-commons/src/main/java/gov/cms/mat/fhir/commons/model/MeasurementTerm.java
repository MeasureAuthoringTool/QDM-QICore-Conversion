package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;

import java.io.Serializable;


@Entity
@Table(name = "MEASUREMENT_TERM")

@NamedQueries({
        @NamedQuery(name = "MeasurementTerm.findAll", query = "SELECT m FROM MeasurementTerm m"),
        @NamedQuery(name = "MeasurementTerm.findById", query = "SELECT m FROM MeasurementTerm m WHERE m.id = :id"),
        @NamedQuery(name = "MeasurementTerm.findByUnit", query = "SELECT m FROM MeasurementTerm m WHERE m.unit = :unit"),
        @NamedQuery(name = "MeasurementTerm.findByQuantity", query = "SELECT m FROM MeasurementTerm m WHERE m.quantity = :quantity")})
public class MeasurementTerm implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "UNIT")
    private String unit;
    @Basic(optional = false)
    @Column(name = "QUANTITY")
    private String quantity;
    @JoinColumn(name = "DECISION_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Decision decisionId;

    public MeasurementTerm() {
    }

    public MeasurementTerm(String id) {
        this.id = id;
    }

    public MeasurementTerm(String id, String quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public Decision getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(Decision decisionId) {
        this.decisionId = decisionId;
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
        if (!(object instanceof MeasurementTerm)) {
            return false;
        }
        MeasurementTerm other = (MeasurementTerm) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasurementTerm[ id=" + id + " ]";
    }

}
