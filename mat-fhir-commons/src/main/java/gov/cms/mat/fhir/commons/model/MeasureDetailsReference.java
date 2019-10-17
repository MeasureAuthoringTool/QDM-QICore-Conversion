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
import javax.persistence.Lob;
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
@Table(name = "MEASURE_DETAILS_REFERENCE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MeasureDetailsReference.findAll", query = "SELECT m FROM MeasureDetailsReference m"),
    @NamedQuery(name = "MeasureDetailsReference.findById", query = "SELECT m FROM MeasureDetailsReference m WHERE m.id = :id"),
    @NamedQuery(name = "MeasureDetailsReference.findByReferenceNumber", query = "SELECT m FROM MeasureDetailsReference m WHERE m.referenceNumber = :referenceNumber")})
public class MeasureDetailsReference implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Lob
    @Column(name = "REFERENCE")
    private String reference;
    @Column(name = "REFERENCE_NUMBER")
    private Integer referenceNumber;
    @JoinColumn(name = "MEASURE_DETAILS_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private MeasureDetails measureDetailsId;

    public MeasureDetailsReference() {
    }

    public MeasureDetailsReference(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Integer getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(Integer referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public MeasureDetails getMeasureDetailsId() {
        return measureDetailsId;
    }

    public void setMeasureDetailsId(MeasureDetails measureDetailsId) {
        this.measureDetailsId = measureDetailsId;
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
        if (!(object instanceof MeasureDetailsReference)) {
            return false;
        }
        MeasureDetailsReference other = (MeasureDetailsReference) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.MeasureDetailsReference[ id=" + id + " ]";
    }
    
}
