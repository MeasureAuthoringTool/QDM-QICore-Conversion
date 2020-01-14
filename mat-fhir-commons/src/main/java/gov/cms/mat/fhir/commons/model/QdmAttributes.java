package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "QDM_ATTRIBUTES")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "QdmAttributes.findAll", query = "SELECT q FROM QdmAttributes q"),
    @NamedQuery(name = "QdmAttributes.findById", query = "SELECT q FROM QdmAttributes q WHERE q.id = :id"),
    @NamedQuery(name = "QdmAttributes.findByName", query = "SELECT q FROM QdmAttributes q WHERE q.name = :name"),
    @NamedQuery(name = "QdmAttributes.findByDataTypeId", query = "SELECT q FROM QdmAttributes q WHERE q.dataTypeId = :dataTypeId"),
    @NamedQuery(name = "QdmAttributes.findByQdmAttributeType", query = "SELECT q FROM QdmAttributes q WHERE q.qdmAttributeType = :qdmAttributeType")})
public class QdmAttributes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "NAME")
    private String name;
    @Basic(optional = false)
    @Column(name = "DATA_TYPE_ID")
    private String dataTypeId;
    @Basic(optional = false)
    @Column(name = "QDM_ATTRIBUTE_TYPE")
    private String qdmAttributeType;

    public QdmAttributes() {
    }

    public QdmAttributes(Integer id) {
        this.id = id;
    }

    public QdmAttributes(Integer id, String name, String dataTypeId, String qdmAttributeType) {
        this.id = id;
        this.name = name;
        this.dataTypeId = dataTypeId;
        this.qdmAttributeType = qdmAttributeType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(String dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public String getQdmAttributeType() {
        return qdmAttributeType;
    }

    public void setQdmAttributeType(String qdmAttributeType) {
        this.qdmAttributeType = qdmAttributeType;
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
        if (!(object instanceof QdmAttributes)) {
            return false;
        }
        QdmAttributes other = (QdmAttributes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.QdmAttributes[ id=" + id + " ]";
    }
    
}
