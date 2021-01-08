package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "CLAUSE")
@NamedQueries({
        @NamedQuery(name = "Clause.findAll", query = "SELECT c FROM Clause c"),
        @NamedQuery(name = "Clause.findById", query = "SELECT c FROM Clause c WHERE c.id = :id"),
        @NamedQuery(name = "Clause.findByName", query = "SELECT c FROM Clause c WHERE c.name = :name"),
        @NamedQuery(name = "Clause.findByCustomName", query = "SELECT c FROM Clause c WHERE c.customName = :customName"),
        @NamedQuery(name = "Clause.findByDescription", query = "SELECT c FROM Clause c WHERE c.description = :description"),
        @NamedQuery(name = "Clause.findByDecisionId", query = "SELECT c FROM Clause c WHERE c.decisionId = :decisionId"),
        @NamedQuery(name = "Clause.findByClauseTypeId", query = "SELECT c FROM Clause c WHERE c.clauseTypeId = :clauseTypeId"),
        @NamedQuery(name = "Clause.findByVersion", query = "SELECT c FROM Clause c WHERE c.version = :version")})
public class Clause implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private String id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "CUSTOM_NAME")
    private String customName;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "DECISION_ID")
    private String decisionId;
    @Column(name = "CLAUSE_TYPE_ID")
    private String clauseTypeId;
    @Column(name = "VERSION")
    private String version;
    @OneToMany(mappedBy = "clauseId")
    private Collection<Decision> decisionCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "clauseId")
    private Collection<Packager> packagerCollection;
    @JoinColumn(name = "CONTEXT_ID", referencedColumnName = "CONTEXT_ID")
    @ManyToOne
    private Context contextId;
    @JoinColumn(name = "MEASURE_ID", referencedColumnName = "ID")
    @ManyToOne
    private Measure measureId;

    public Clause() {
    }

    public Clause(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    public String getClauseTypeId() {
        return clauseTypeId;
    }

    public void setClauseTypeId(String clauseTypeId) {
        this.clauseTypeId = clauseTypeId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public Collection<Decision> getDecisionCollection() {
        return decisionCollection;
    }

    public void setDecisionCollection(Collection<Decision> decisionCollection) {
        this.decisionCollection = decisionCollection;
    }


    public Collection<Packager> getPackagerCollection() {
        return packagerCollection;
    }

    public void setPackagerCollection(Collection<Packager> packagerCollection) {
        this.packagerCollection = packagerCollection;
    }

    public Context getContextId() {
        return contextId;
    }

    public void setContextId(Context contextId) {
        this.contextId = contextId;
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
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Clause)) {
            return false;
        }
        Clause other = (Clause) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Clause[ id=" + id + " ]";
    }

}
