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
@Table(name = "GROUPED_CODE_LISTS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "GroupedCodeLists.findAll", query = "SELECT g FROM GroupedCodeLists g"),
    @NamedQuery(name = "GroupedCodeLists.findByGroupedCodeListsId", query = "SELECT g FROM GroupedCodeLists g WHERE g.groupedCodeListsId = :groupedCodeListsId"),
    @NamedQuery(name = "GroupedCodeLists.findByDescription", query = "SELECT g FROM GroupedCodeLists g WHERE g.description = :description")})
public class GroupedCodeLists implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "GROUPED_CODE_LISTS_ID")
    private String groupedCodeListsId;
    @Basic(optional = false)
    @Column(name = "DESCRIPTION")
    private String description;
    @JoinColumn(name = "CODE_LIST_ID", referencedColumnName = "CODE_LIST_ID")
    @ManyToOne(optional = false)
    private CodeList codeListId;
    @JoinColumn(name = "GROUP_LIST_ID", referencedColumnName = "LIST_OBJECT_ID")
    @ManyToOne(optional = false)
    private ListObject groupListId;

    public GroupedCodeLists() {
    }

    public GroupedCodeLists(String groupedCodeListsId) {
        this.groupedCodeListsId = groupedCodeListsId;
    }

    public GroupedCodeLists(String groupedCodeListsId, String description) {
        this.groupedCodeListsId = groupedCodeListsId;
        this.description = description;
    }

    public String getGroupedCodeListsId() {
        return groupedCodeListsId;
    }

    public void setGroupedCodeListsId(String groupedCodeListsId) {
        this.groupedCodeListsId = groupedCodeListsId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CodeList getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(CodeList codeListId) {
        this.codeListId = codeListId;
    }

    public ListObject getGroupListId() {
        return groupListId;
    }

    public void setGroupListId(ListObject groupListId) {
        this.groupListId = groupListId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (groupedCodeListsId != null ? groupedCodeListsId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof GroupedCodeLists)) {
            return false;
        }
        GroupedCodeLists other = (GroupedCodeLists) object;
        if ((this.groupedCodeListsId == null && other.groupedCodeListsId != null) || (this.groupedCodeListsId != null && !this.groupedCodeListsId.equals(other.groupedCodeListsId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.GroupedCodeLists[ groupedCodeListsId=" + groupedCodeListsId + " ]";
    }
    
}
