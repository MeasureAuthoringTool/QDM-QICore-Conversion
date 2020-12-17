package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "MEASURE_SCORE")

@NamedQueries({
        @NamedQuery(name = "MeasureScore.findAll", query = "SELECT m FROM MeasureScore m"),
        @NamedQuery(name = "MeasureScore.findById", query = "SELECT m FROM MeasureScore m WHERE m.id = :id"),
        @NamedQuery(name = "MeasureScore.findByScore", query = "SELECT m FROM MeasureScore m WHERE m.score = :score")})
public class MeasureScore implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "SCORE")
    private String score;

    public MeasureScore() {
    }

    public MeasureScore(String id) {
        this.id = id;
    }

    public MeasureScore(String id, String score) {
        this.id = id;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
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
        if (!(object instanceof MeasureScore)) {
            return false;
        }
        MeasureScore other = (MeasureScore) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureScore[ id=" + id + " ]";
    }

}
