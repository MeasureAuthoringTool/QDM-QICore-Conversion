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
@Table(name = "CQL_DATA")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CqlData.findAll", query = "SELECT c FROM CqlData c"),
    @NamedQuery(name = "CqlData.findById", query = "SELECT c FROM CqlData c WHERE c.id = :id")})
public class CqlData implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Lob
    @Column(name = "CQL_DATA")
    private byte[] cqlData;
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Measure measureId;

    public CqlData() {
    }

    public CqlData(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getCqlData() {
        return cqlData;
    }

    public void setCqlData(byte[] cqlData) {
        this.cqlData = cqlData;
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
        if (!(object instanceof CqlData)) {
            return false;
        }
        CqlData other = (CqlData) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CqlData[ id=" + id + " ]";
    }
    
}
