package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "PACKAGER")

@NamedQueries({
        @NamedQuery(name = "Packager.findAll", query = "SELECT p FROM Packager p"),
        @NamedQuery(name = "Packager.findByPackagerId", query = "SELECT p FROM Packager p WHERE p.packagerId = :packagerId"),
        @NamedQuery(name = "Packager.findBySequence", query = "SELECT p FROM Packager p WHERE p.sequence = :sequence")})
public class Packager implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "PACKAGER_ID")
    private String packagerId;
    @Basic(optional = false)
    @Column(name = "SEQUENCE")
    private int sequence;
    @JoinColumn(name = "CLAUSE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Clause clauseId;
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Measure measureId;

    public Packager() {
    }

    public Packager(String packagerId) {
        this.packagerId = packagerId;
    }

    public Packager(String packagerId, int sequence) {
        this.packagerId = packagerId;
        this.sequence = sequence;
    }

    public String getPackagerId() {
        return packagerId;
    }

    public void setPackagerId(String packagerId) {
        this.packagerId = packagerId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Clause getClauseId() {
        return clauseId;
    }

    public void setClauseId(Clause clauseId) {
        this.clauseId = clauseId;
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
        hash += (packagerId != null ? packagerId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Packager)) {
            return false;
        }
        Packager other = (Packager) object;
        if ((this.packagerId == null && other.packagerId != null) || (this.packagerId != null && !this.packagerId.equals(other.packagerId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Packager[ packagerId=" + packagerId + " ]";
    }

}
