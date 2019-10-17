/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "CQL_LIBRARY_HISTORY")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CqlLibraryHistory.findAll", query = "SELECT c FROM CqlLibraryHistory c"),
    @NamedQuery(name = "CqlLibraryHistory.findById", query = "SELECT c FROM CqlLibraryHistory c WHERE c.id = :id"),
    @NamedQuery(name = "CqlLibraryHistory.findByLastModifiedOn", query = "SELECT c FROM CqlLibraryHistory c WHERE c.lastModifiedOn = :lastModifiedOn"),
    @NamedQuery(name = "CqlLibraryHistory.findByFreeTextEditorUsed", query = "SELECT c FROM CqlLibraryHistory c WHERE c.freeTextEditorUsed = :freeTextEditorUsed")})
public class CqlLibraryHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Lob
    @Column(name = "CQL_LIBRARY")
    private byte[] cqlLibrary;
    @Basic(optional = false)
    @Column(name = "LAST_MODIFIED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedOn;
    @Column(name = "FREE_TEXT_EDITOR_USED")
    private Boolean freeTextEditorUsed;
    @JoinColumn(name = "LIBRARY_ID", referencedColumnName = "ID")
    @ManyToOne
    private CqlLibrary libraryId;
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne
    private Measure measureId;
    @JoinColumn(name = "LAST_MODIFIED_BY", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User lastModifiedBy;

    public CqlLibraryHistory() {
    }

    public CqlLibraryHistory(Integer id) {
        this.id = id;
    }

    public CqlLibraryHistory(Integer id, Date lastModifiedOn) {
        this.id = id;
        this.lastModifiedOn = lastModifiedOn;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getCqlLibrary() {
        return cqlLibrary;
    }

    public void setCqlLibrary(byte[] cqlLibrary) {
        this.cqlLibrary = cqlLibrary;
    }

    public Date getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(Date lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    public Boolean getFreeTextEditorUsed() {
        return freeTextEditorUsed;
    }

    public void setFreeTextEditorUsed(Boolean freeTextEditorUsed) {
        this.freeTextEditorUsed = freeTextEditorUsed;
    }

    public CqlLibrary getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(CqlLibrary libraryId) {
        this.libraryId = libraryId;
    }

    public Measure getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Measure measureId) {
        this.measureId = measureId;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
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
        if (!(object instanceof CqlLibraryHistory)) {
            return false;
        }
        CqlLibraryHistory other = (CqlLibraryHistory) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CqlLibraryHistory[ id=" + id + " ]";
    }
    
}
