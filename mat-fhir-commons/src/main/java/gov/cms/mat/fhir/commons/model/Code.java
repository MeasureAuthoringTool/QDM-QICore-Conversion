package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "CODE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Code.findAll", query = "SELECT c FROM Code c"),
    @NamedQuery(name = "Code.findByCodeId", query = "SELECT c FROM Code c WHERE c.codeId = :codeId"),
    @NamedQuery(name = "Code.findByCode", query = "SELECT c FROM Code c WHERE c.code = :code"),
    @NamedQuery(name = "Code.findByDescription", query = "SELECT c FROM Code c WHERE c.description = :description")})
public class Code implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "CODE_ID")
    private String codeId;
    @Basic(optional = false)
    @Column(name = "CODE")
    private String code;
    @Basic(optional = false)
    @Column(name = "DESCRIPTION")
    private String description;
    @JoinColumn(name = "CODE_LIST_ID", referencedColumnName = "CODE_LIST_ID")
    @ManyToOne(optional = false)
    private CodeList codeListId;

    public Code() {
    }

    public Code(String codeId) {
        this.codeId = codeId;
    }

    public Code(String codeId, String code, String description) {
        this.codeId = codeId;
        this.code = code;
        this.description = description;
    }

    public String getCodeId() {
        return codeId;
    }

    public void setCodeId(String codeId) {
        this.codeId = codeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codeId != null ? codeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Code)) {
            return false;
        }
        Code other = (Code) object;
        if ((this.codeId == null && other.codeId != null) || (this.codeId != null && !this.codeId.equals(other.codeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Code[ codeId=" + codeId + " ]";
    }
    
}
