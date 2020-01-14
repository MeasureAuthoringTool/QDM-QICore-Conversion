package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "ATTRIBUTE_DETAILS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AttributeDetails.findAll", query = "SELECT a FROM AttributeDetails a"),
    @NamedQuery(name = "AttributeDetails.findByAttributeDetailsId", query = "SELECT a FROM AttributeDetails a WHERE a.attributeDetailsId = :attributeDetailsId"),
    @NamedQuery(name = "AttributeDetails.findByAttrName", query = "SELECT a FROM AttributeDetails a WHERE a.attrName = :attrName"),
    @NamedQuery(name = "AttributeDetails.findByCode", query = "SELECT a FROM AttributeDetails a WHERE a.code = :code"),
    @NamedQuery(name = "AttributeDetails.findByCodeSystem", query = "SELECT a FROM AttributeDetails a WHERE a.codeSystem = :codeSystem"),
    @NamedQuery(name = "AttributeDetails.findByCodeSystemName", query = "SELECT a FROM AttributeDetails a WHERE a.codeSystemName = :codeSystemName"),
    @NamedQuery(name = "AttributeDetails.findByMode", query = "SELECT a FROM AttributeDetails a WHERE a.mode = :mode"),
    @NamedQuery(name = "AttributeDetails.findByTypeCode", query = "SELECT a FROM AttributeDetails a WHERE a.typeCode = :typeCode")})
public class AttributeDetails implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ATTRIBUTE_DETAILS_ID")
    private String attributeDetailsId;
    @Basic(optional = false)
    @Column(name = "ATTR_NAME")
    private String attrName;
    @Basic(optional = false)
    @Column(name = "CODE")
    private String code;
    @Basic(optional = false)
    @Column(name = "CODE_SYSTEM")
    private String codeSystem;
    @Basic(optional = false)
    @Column(name = "CODE_SYSTEM_NAME")
    private String codeSystemName;
    @Basic(optional = false)
    @Column(name = "MODE")
    private String mode;
    @Basic(optional = false)
    @Column(name = "TYPE_CODE")
    private String typeCode;

    public AttributeDetails() {
    }

    public AttributeDetails(String attributeDetailsId) {
        this.attributeDetailsId = attributeDetailsId;
    }

    public AttributeDetails(String attributeDetailsId, String attrName, String code, String codeSystem, String codeSystemName, String mode, String typeCode) {
        this.attributeDetailsId = attributeDetailsId;
        this.attrName = attrName;
        this.code = code;
        this.codeSystem = codeSystem;
        this.codeSystemName = codeSystemName;
        this.mode = mode;
        this.typeCode = typeCode;
    }

    public String getAttributeDetailsId() {
        return attributeDetailsId;
    }

    public void setAttributeDetailsId(String attributeDetailsId) {
        this.attributeDetailsId = attributeDetailsId;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public void setCodeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (attributeDetailsId != null ? attributeDetailsId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AttributeDetails)) {
            return false;
        }
        AttributeDetails other = (AttributeDetails) object;
        if ((this.attributeDetailsId == null && other.attributeDetailsId != null) || (this.attributeDetailsId != null && !this.attributeDetailsId.equals(other.attributeDetailsId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.AttributeDetails[ attributeDetailsId=" + attributeDetailsId + " ]";
    }
    
}
