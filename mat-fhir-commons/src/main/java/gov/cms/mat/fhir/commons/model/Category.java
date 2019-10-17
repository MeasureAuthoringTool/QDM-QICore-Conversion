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
@Table(name = "CATEGORY")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Category.findAll", query = "SELECT c FROM Category c"),
    @NamedQuery(name = "Category.findByCategoryId", query = "SELECT c FROM Category c WHERE c.categoryId = :categoryId"),
    @NamedQuery(name = "Category.findByDescription", query = "SELECT c FROM Category c WHERE c.description = :description"),
    @NamedQuery(name = "Category.findByAbbreviation", query = "SELECT c FROM Category c WHERE c.abbreviation = :abbreviation")})
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "CATEGORY_ID")
    private String categoryId;
    @Basic(optional = false)
    @Column(name = "DESCRIPTION")
    private String description;
    @Basic(optional = false)
    @Column(name = "ABBREVIATION")
    private String abbreviation;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "categoryId")
    private Collection<DataType> dataTypeCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "categoryId")
    private Collection<CodeSystem> codeSystemCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "categoryId")
    private Collection<ListObject> listObjectCollection;

    public Category() {
    }

    public Category(String categoryId) {
        this.categoryId = categoryId;
    }

    public Category(String categoryId, String description, String abbreviation) {
        this.categoryId = categoryId;
        this.description = description;
        this.abbreviation = abbreviation;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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

    @XmlTransient
    public Collection<DataType> getDataTypeCollection() {
        return dataTypeCollection;
    }

    public void setDataTypeCollection(Collection<DataType> dataTypeCollection) {
        this.dataTypeCollection = dataTypeCollection;
    }

    @XmlTransient
    public Collection<CodeSystem> getCodeSystemCollection() {
        return codeSystemCollection;
    }

    public void setCodeSystemCollection(Collection<CodeSystem> codeSystemCollection) {
        this.codeSystemCollection = codeSystemCollection;
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
        hash += (categoryId != null ? categoryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Category)) {
            return false;
        }
        Category other = (Category) object;
        if ((this.categoryId == null && other.categoryId != null) || (this.categoryId != null && !this.categoryId.equals(other.categoryId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Category[ categoryId=" + categoryId + " ]";
    }
    
}
