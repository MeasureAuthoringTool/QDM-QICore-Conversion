package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;


import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "DATA_TYPE")

@NamedQueries({
        @NamedQuery(name = "DataType.findAll", query = "SELECT d FROM DataType d"),
        @NamedQuery(name = "DataType.findByDataTypeId", query = "SELECT d FROM DataType d WHERE d.dataTypeId = :dataTypeId"),
        @NamedQuery(name = "DataType.findByDescription", query = "SELECT d FROM DataType d WHERE d.description = :description")})
public class DataType implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "DATA_TYPE_ID")
    private String dataTypeId;
    @Basic(optional = false)
    @Column(name = "DESCRIPTION")
    private String description;
    @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "CATEGORY_ID")
    @ManyToOne(optional = false)
    private Category categoryId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataTypeId")
    private Collection<QualityDataModel> qualityDataModelCollection;

    public DataType() {
    }

    public DataType(String dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public DataType(String dataTypeId, String description) {
        this.dataTypeId = dataTypeId;
        this.description = description;
    }

    public String getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(String dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
    }


    public Collection<QualityDataModel> getQualityDataModelCollection() {
        return qualityDataModelCollection;
    }

    public void setQualityDataModelCollection(Collection<QualityDataModel> qualityDataModelCollection) {
        this.qualityDataModelCollection = qualityDataModelCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dataTypeId != null ? dataTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DataType)) {
            return false;
        }
        DataType other = (DataType) object;
        if ((this.dataTypeId == null && other.dataTypeId != null) || (this.dataTypeId != null && !this.dataTypeId.equals(other.dataTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.DataType[ dataTypeId=" + dataTypeId + " ]";
    }

}
