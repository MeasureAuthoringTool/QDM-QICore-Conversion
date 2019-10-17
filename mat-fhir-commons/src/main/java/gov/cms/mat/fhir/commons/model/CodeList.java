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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "CODE_LIST")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CodeList.findAll", query = "SELECT c FROM CodeList c"),
    @NamedQuery(name = "CodeList.findByCodeListId", query = "SELECT c FROM CodeList c WHERE c.codeListId = :codeListId")})
public class CodeList implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "CODE_LIST_ID")
    private String codeListId;
    @JoinColumn(name = "CODE_LIST_ID", referencedColumnName = "LIST_OBJECT_ID", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private ListObject listObject;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "codeListId")
    private Collection<GroupedCodeLists> groupedCodeListsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "codeListId")
    private Collection<Code> codeCollection;

    public CodeList() {
    }

    public CodeList(String codeListId) {
        this.codeListId = codeListId;
    }

    public String getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(String codeListId) {
        this.codeListId = codeListId;
    }

    public ListObject getListObject() {
        return listObject;
    }

    public void setListObject(ListObject listObject) {
        this.listObject = listObject;
    }

    @XmlTransient
    public Collection<GroupedCodeLists> getGroupedCodeListsCollection() {
        return groupedCodeListsCollection;
    }

    public void setGroupedCodeListsCollection(Collection<GroupedCodeLists> groupedCodeListsCollection) {
        this.groupedCodeListsCollection = groupedCodeListsCollection;
    }

    @XmlTransient
    public Collection<Code> getCodeCollection() {
        return codeCollection;
    }

    public void setCodeCollection(Collection<Code> codeCollection) {
        this.codeCollection = codeCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codeListId != null ? codeListId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CodeList)) {
            return false;
        }
        CodeList other = (CodeList) object;
        if ((this.codeListId == null && other.codeListId != null) || (this.codeListId != null && !this.codeListId.equals(other.codeListId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CodeList[ codeListId=" + codeListId + " ]";
    }
    
}
