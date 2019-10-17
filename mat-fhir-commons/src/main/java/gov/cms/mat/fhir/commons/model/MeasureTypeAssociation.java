/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "MEASURE_TYPE_ASSOCIATION")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MeasureTypeAssociation.findAll", query = "SELECT m FROM MeasureTypeAssociation m"),
    @NamedQuery(name = "MeasureTypeAssociation.findById", query = "SELECT m FROM MeasureTypeAssociation m WHERE m.id = :id")})
public class MeasureTypeAssociation implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Measure measureId;
    @JoinColumn(name = "MEASURE_TYPE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private MeasureTypes measureTypeId;

    public MeasureTypeAssociation() {
    }

    public MeasureTypeAssociation(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Measure getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Measure measureId) {
        this.measureId = measureId;
    }

    public MeasureTypes getMeasureTypeId() {
        return measureTypeId;
    }

    public void setMeasureTypeId(MeasureTypes measureTypeId) {
        this.measureTypeId = measureTypeId;
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
        if (!(object instanceof MeasureTypeAssociation)) {
            return false;
        }
        MeasureTypeAssociation other = (MeasureTypeAssociation) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureTypeAssociation[ id=" + id + " ]";
    }
    
}
