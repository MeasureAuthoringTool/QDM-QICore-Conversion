package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "CQL_LIBRARY_EXPORT")

public class CqlLibraryExport implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;

    @Lob
    @Column(name = "CQL")
    private byte[] cql;

    @Lob
    @Column(name = "ELM")
    private byte[] elm;

    @Lob
    @Column(name = "JSON")
    private byte[] json;

    @Column(name = "CQL_LIBRARY_ID")
    private String cqlLibraryId;

    public CqlLibraryExport() {
    }

    public CqlLibraryExport(String id) {
        this.id = id;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getCql() {
        return cql;
    }

    public void setCql(byte[] cql) {
        this.cql = cql;
    }

    public byte[] getElm() {
        return elm;
    }

    public void setElm(byte[] elm) {
        this.elm = elm;
    }

    public byte[] getJson() {
        return json;
    }

    public void setJson(byte[] json) {
        this.json = json;
    }

    public String getCqlLibraryId() {
        return cqlLibraryId;
    }

    public void setCqlLibraryId(String cqlLibraryId) {
        this.cqlLibraryId = cqlLibraryId;
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
        if (!(object instanceof CqlLibraryExport)) {
            return false;
        }
        CqlLibraryExport other = (CqlLibraryExport) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CqlLibraryExport[ id=" + id + " ]";
    }

}
