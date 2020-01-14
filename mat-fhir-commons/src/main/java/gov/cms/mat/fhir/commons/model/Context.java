package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "CONTEXT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Context.findAll", query = "SELECT c FROM Context c"),
    @NamedQuery(name = "Context.findByContextId", query = "SELECT c FROM Context c WHERE c.contextId = :contextId"),
    @NamedQuery(name = "Context.findByDescription", query = "SELECT c FROM Context c WHERE c.description = :description")})
public class Context implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "CONTEXT_ID")
    private String contextId;
    @Basic(optional = false)
    @Column(name = "DESCRIPTION")
    private String description;
    @OneToMany(mappedBy = "contextId")
    private Collection<Clause> clauseCollection;

    public Context() {
    }

    public Context(String contextId) {
        this.contextId = contextId;
    }

    public Context(String contextId, String description) {
        this.contextId = contextId;
        this.description = description;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    public Collection<Clause> getClauseCollection() {
        return clauseCollection;
    }

    public void setClauseCollection(Collection<Clause> clauseCollection) {
        this.clauseCollection = clauseCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (contextId != null ? contextId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Context)) {
            return false;
        }
        Context other = (Context) object;
        if ((this.contextId == null && other.contextId != null) || (this.contextId != null && !this.contextId.equals(other.contextId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Context[ contextId=" + contextId + " ]";
    }
    
}
