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
@Table(name = "OPERATOR")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Operator.findAll", query = "SELECT o FROM Operator o"),
    @NamedQuery(name = "Operator.findById", query = "SELECT o FROM Operator o WHERE o.id = :id"),
    @NamedQuery(name = "Operator.findByLongName", query = "SELECT o FROM Operator o WHERE o.longName = :longName"),
    @NamedQuery(name = "Operator.findByShortName", query = "SELECT o FROM Operator o WHERE o.shortName = :shortName")})
public class Operator implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "LONG_NAME")
    private String longName;
    @Column(name = "SHORT_NAME")
    private String shortName;
    @JoinColumn(name = "FK_OPERATOR_TYPE", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private OperatorType fkOperatorType;

    public Operator() {
    }

    public Operator(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public OperatorType getFkOperatorType() {
        return fkOperatorType;
    }

    public void setFkOperatorType(OperatorType fkOperatorType) {
        this.fkOperatorType = fkOperatorType;
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
        if (!(object instanceof Operator)) {
            return false;
        }
        Operator other = (Operator) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Operator[ id=" + id + " ]";
    }
    
}
