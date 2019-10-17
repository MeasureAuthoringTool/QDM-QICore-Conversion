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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "QDM_TERM")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "QdmTerm.findAll", query = "SELECT q FROM QdmTerm q"),
    @NamedQuery(name = "QdmTerm.findById", query = "SELECT q FROM QdmTerm q WHERE q.id = :id"),
    @NamedQuery(name = "QdmTerm.findByQdmElementId", query = "SELECT q FROM QdmTerm q WHERE q.qdmElementId = :qdmElementId"),
    @NamedQuery(name = "QdmTerm.findByDecisionId", query = "SELECT q FROM QdmTerm q WHERE q.decisionId = :decisionId")})
public class QdmTerm implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "QDM_ELEMENT_ID")
    private String qdmElementId;
    @Basic(optional = false)
    @Column(name = "DECISION_ID")
    private String decisionId;

    public QdmTerm() {
    }

    public QdmTerm(String id) {
        this.id = id;
    }

    public QdmTerm(String id, String qdmElementId, String decisionId) {
        this.id = id;
        this.qdmElementId = qdmElementId;
        this.decisionId = decisionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQdmElementId() {
        return qdmElementId;
    }

    public void setQdmElementId(String qdmElementId) {
        this.qdmElementId = qdmElementId;
    }

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
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
        if (!(object instanceof QdmTerm)) {
            return false;
        }
        QdmTerm other = (QdmTerm) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.QdmTerm[ id=" + id + " ]";
    }
    
}
