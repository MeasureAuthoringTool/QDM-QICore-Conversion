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
@Table(name = "CODE_SYSTEM")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CodeSystem.findAll", query = "SELECT c FROM CodeSystem c"),
    @NamedQuery(name = "CodeSystem.findByCodeSystemId", query = "SELECT c FROM CodeSystem c WHERE c.codeSystemId = :codeSystemId"),
    @NamedQuery(name = "CodeSystem.findByDescription", query = "SELECT c FROM CodeSystem c WHERE c.description = :description"),
    @NamedQuery(name = "CodeSystem.findByAbbreviation", query = "SELECT c FROM CodeSystem c WHERE c.abbreviation = :abbreviation")})
public class CodeSystem implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "CODE_SYSTEM_ID")
    private String codeSystemId;
    @Basic(optional = false)
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "ABBREVIATION")
    private String abbreviation;
    @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "CATEGORY_ID")
    @ManyToOne(optional = false)
    private Category categoryId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "codeSystemId")
    private Collection<ListObject> listObjectCollection;

    public CodeSystem() {
    }

    public CodeSystem(String codeSystemId) {
        this.codeSystemId = codeSystemId;
    }

    public CodeSystem(String codeSystemId, String description) {
        this.codeSystemId = codeSystemId;
        this.description = description;
    }

    public String getCodeSystemId() {
        return codeSystemId;
    }

    public void setCodeSystemId(String codeSystemId) {
        this.codeSystemId = codeSystemId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public Category getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
    }

    @XmlTransient
    public Collection<ListObject> getListObjectCollection() {
        return listObjectCollection;
    }

    public void setListObjectCollection(Collection<ListObject> listObjectCollection) {
        this.listObjectCollection = listObjectCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codeSystemId != null ? codeSystemId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CodeSystem)) {
            return false;
        }
        CodeSystem other = (CodeSystem) object;
        if ((this.codeSystemId == null && other.codeSystemId != null) || (this.codeSystemId != null && !this.codeSystemId.equals(other.codeSystemId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CodeSystem[ codeSystemId=" + codeSystemId + " ]";
    }
    
}
