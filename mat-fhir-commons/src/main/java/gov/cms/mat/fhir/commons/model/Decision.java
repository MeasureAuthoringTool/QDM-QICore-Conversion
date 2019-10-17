/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "DECISION")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Decision.findAll", query = "SELECT d FROM Decision d"),
    @NamedQuery(name = "Decision.findById", query = "SELECT d FROM Decision d WHERE d.id = :id"),
    @NamedQuery(name = "Decision.findByOperator", query = "SELECT d FROM Decision d WHERE d.operator = :operator"),
    @NamedQuery(name = "Decision.findByOrderNum", query = "SELECT d FROM Decision d WHERE d.orderNum = :orderNum"),
    @NamedQuery(name = "Decision.findByAttributeId", query = "SELECT d FROM Decision d WHERE d.attributeId = :attributeId")})
public class Decision implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "OPERATOR")
    private String operator;
    @Column(name = "ORDER_NUM")
    private String orderNum;
    @Column(name = "ATTRIBUTE_ID")
    private String attributeId;
    @JoinColumn(name = "CLAUSE_ID", referencedColumnName = "ID")
    @ManyToOne
    private Clause clauseId;
    @OneToMany(mappedBy = "parentId")
    private Collection<Decision> decisionCollection;
    @JoinColumn(name = "PARENT_ID", referencedColumnName = "ID")
    @ManyToOne
    private Decision parentId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "decisionId")
    private Collection<MeasurementTerm> measurementTermCollection;

    public Decision() {
    }

    public Decision(String id) {
        this.id = id;
    }

    public Decision(String id, String operator) {
        this.id = id;
        this.operator = operator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public Clause getClauseId() {
        return clauseId;
    }

    public void setClauseId(Clause clauseId) {
        this.clauseId = clauseId;
    }

    @XmlTransient
    public Collection<Decision> getDecisionCollection() {
        return decisionCollection;
    }

    public void setDecisionCollection(Collection<Decision> decisionCollection) {
        this.decisionCollection = decisionCollection;
    }

    public Decision getParentId() {
        return parentId;
    }

    public void setParentId(Decision parentId) {
        this.parentId = parentId;
    }

    @XmlTransient
    public Collection<MeasurementTerm> getMeasurementTermCollection() {
        return measurementTermCollection;
    }

    public void setMeasurementTermCollection(Collection<MeasurementTerm> measurementTermCollection) {
        this.measurementTermCollection = measurementTermCollection;
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
        if (!(object instanceof Decision)) {
            return false;
        }
        Decision other = (Decision) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Decision[ id=" + id + " ]";
    }
    
}
