package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "QUALITY_DATA_MODEL")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "QualityDataModel.findAll", query = "SELECT q FROM QualityDataModel q"),
    @NamedQuery(name = "QualityDataModel.findByQualityDataModelId", query = "SELECT q FROM QualityDataModel q WHERE q.qualityDataModelId = :qualityDataModelId"),
    @NamedQuery(name = "QualityDataModel.findByVersion", query = "SELECT q FROM QualityDataModel q WHERE q.version = :version"),
    @NamedQuery(name = "QualityDataModel.findByOid", query = "SELECT q FROM QualityDataModel q WHERE q.oid = :oid"),
    @NamedQuery(name = "QualityDataModel.findByOccurrence", query = "SELECT q FROM QualityDataModel q WHERE q.occurrence = :occurrence"),
    @NamedQuery(name = "QualityDataModel.findByIsSuppDataElement", query = "SELECT q FROM QualityDataModel q WHERE q.isSuppDataElement = :isSuppDataElement")})
public class QualityDataModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "QUALITY_DATA_MODEL_ID")
    private String qualityDataModelId;
    @Basic(optional = false)
    @Column(name = "VERSION")
    private String version;
    @Column(name = "OID")
    private String oid;
    @Column(name = "OCCURRENCE")
    private String occurrence;
    @Basic(optional = false)
    @Column(name = "IS_SUPP_DATA_ELEMENT")
    private boolean isSuppDataElement;
    @JoinColumn(name = "LIST_OBJECT_ID", referencedColumnName = "LIST_OBJECT_ID")
    @ManyToOne(optional = false)
    private ListObject listObjectId;
    @JoinColumn(name = "DATA_TYPE_ID", referencedColumnName = "DATA_TYPE_ID")
    @ManyToOne(optional = false)
    private DataType dataTypeId;
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Measure measureId;

    public QualityDataModel() {
    }

    public QualityDataModel(String qualityDataModelId) {
        this.qualityDataModelId = qualityDataModelId;
    }

    public QualityDataModel(String qualityDataModelId, String version, boolean isSuppDataElement) {
        this.qualityDataModelId = qualityDataModelId;
        this.version = version;
        this.isSuppDataElement = isSuppDataElement;
    }

    public String getQualityDataModelId() {
        return qualityDataModelId;
    }

    public void setQualityDataModelId(String qualityDataModelId) {
        this.qualityDataModelId = qualityDataModelId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(String occurrence) {
        this.occurrence = occurrence;
    }

    public boolean getIsSuppDataElement() {
        return isSuppDataElement;
    }

    public void setIsSuppDataElement(boolean isSuppDataElement) {
        this.isSuppDataElement = isSuppDataElement;
    }

    public ListObject getListObjectId() {
        return listObjectId;
    }

    public void setListObjectId(ListObject listObjectId) {
        this.listObjectId = listObjectId;
    }

    public DataType getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(DataType dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public Measure getMeasureId() {
        return measureId;
    }

    public void setMeasureId(Measure measureId) {
        this.measureId = measureId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (qualityDataModelId != null ? qualityDataModelId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof QualityDataModel)) {
            return false;
        }
        QualityDataModel other = (QualityDataModel) object;
        if ((this.qualityDataModelId == null && other.qualityDataModelId != null) || (this.qualityDataModelId != null && !this.qualityDataModelId.equals(other.qualityDataModelId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.QualityDataModel[ qualityDataModelId=" + qualityDataModelId + " ]";
    }
    
}
