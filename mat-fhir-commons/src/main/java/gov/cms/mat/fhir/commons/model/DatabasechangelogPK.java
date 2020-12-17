package gov.cms.mat.fhir.commons.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class DatabasechangelogPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Basic(optional = false)
    @Column(name = "AUTHOR")
    private String author;
    @Basic(optional = false)
    @Column(name = "FILENAME")
    private String filename;

    public DatabasechangelogPK() {
    }

    public DatabasechangelogPK(String id, String author, String filename) {
        this.id = id;
        this.author = author;
        this.filename = filename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        hash += (author != null ? author.hashCode() : 0);
        hash += (filename != null ? filename.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatabasechangelogPK)) {
            return false;
        }
        DatabasechangelogPK other = (DatabasechangelogPK) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        if ((this.author == null && other.author != null) || (this.author != null && !this.author.equals(other.author))) {
            return false;
        }
        if ((this.filename == null && other.filename != null) || (this.filename != null && !this.filename.equals(other.filename))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.DatabasechangelogPK[ id=" + id + ", author=" + author + ", filename=" + filename + " ]";
    }

}
