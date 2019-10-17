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
@Table(name = "CQL_LIBRARY_EXPORT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CqlLibraryExport.findAll", query = "SELECT c FROM CqlLibraryExport c"),
    @NamedQuery(name = "CqlLibraryExport.findById", query = "SELECT c FROM CqlLibraryExport c WHERE c.id = :id")})
public class CqlLibraryExport implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Lob
    @Column(name = "CQL")
    private String cql;
    @Lob
    @Column(name = "ELM")
    private String elm;
    @Lob
    @Column(name = "JSON")
    private String json;
    @JoinColumn(name = "CQL_LIBRARY_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private CqlLibrary cqlLibraryId;

    public CqlLibraryExport() {
    }

    public CqlLibraryExport(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCql() {
        return cql;
    }

    public void setCql(String cql) {
        this.cql = cql;
    }

    public String getElm() {
        return elm;
    }

    public void setElm(String elm) {
        this.elm = elm;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public CqlLibrary getCqlLibraryId() {
        return cqlLibraryId;
    }

    public void setCqlLibraryId(CqlLibrary cqlLibraryId) {
        this.cqlLibraryId = cqlLibraryId;
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
        if (!(object instanceof CqlLibraryExport)) {
            return false;
        }
        CqlLibraryExport other = (CqlLibraryExport) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CqlLibraryExport[ id=" + id + " ]";
    }
    
}
