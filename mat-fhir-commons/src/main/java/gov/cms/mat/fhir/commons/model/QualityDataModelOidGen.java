package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "QUALITY_DATA_MODEL_OID_GEN")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "QualityDataModelOidGen.findAll", query = "SELECT q FROM QualityDataModelOidGen q"),
    @NamedQuery(name = "QualityDataModelOidGen.findByOidGenId", query = "SELECT q FROM QualityDataModelOidGen q WHERE q.oidGenId = :oidGenId")})
public class QualityDataModelOidGen implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "OID_GEN_ID")
    private Long oidGenId;

    public QualityDataModelOidGen() {
    }

    public QualityDataModelOidGen(Long oidGenId) {
        this.oidGenId = oidGenId;
    }

    public Long getOidGenId() {
        return oidGenId;
    }

    public void setOidGenId(Long oidGenId) {
        this.oidGenId = oidGenId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (oidGenId != null ? oidGenId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof QualityDataModelOidGen)) {
            return false;
        }
        QualityDataModelOidGen other = (QualityDataModelOidGen) object;
        if ((this.oidGenId == null && other.oidGenId != null) || (this.oidGenId != null && !this.oidGenId.equals(other.oidGenId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.QualityDataModelOidGen[ oidGenId=" + oidGenId + " ]";
    }
    
}
