package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "DATABASECHANGELOG")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Databasechangelog.findAll", query = "SELECT d FROM Databasechangelog d"),
    @NamedQuery(name = "Databasechangelog.findById", query = "SELECT d FROM Databasechangelog d WHERE d.databasechangelogPK.id = :id"),
    @NamedQuery(name = "Databasechangelog.findByAuthor", query = "SELECT d FROM Databasechangelog d WHERE d.databasechangelogPK.author = :author"),
    @NamedQuery(name = "Databasechangelog.findByFilename", query = "SELECT d FROM Databasechangelog d WHERE d.databasechangelogPK.filename = :filename"),
    @NamedQuery(name = "Databasechangelog.findByDateexecuted", query = "SELECT d FROM Databasechangelog d WHERE d.dateexecuted = :dateexecuted"),
    @NamedQuery(name = "Databasechangelog.findByOrderexecuted", query = "SELECT d FROM Databasechangelog d WHERE d.orderexecuted = :orderexecuted"),
    @NamedQuery(name = "Databasechangelog.findByExectype", query = "SELECT d FROM Databasechangelog d WHERE d.exectype = :exectype"),
    @NamedQuery(name = "Databasechangelog.findByMd5sum", query = "SELECT d FROM Databasechangelog d WHERE d.md5sum = :md5sum"),
    @NamedQuery(name = "Databasechangelog.findByDescription", query = "SELECT d FROM Databasechangelog d WHERE d.description = :description"),
    @NamedQuery(name = "Databasechangelog.findByComments", query = "SELECT d FROM Databasechangelog d WHERE d.comments = :comments"),
    @NamedQuery(name = "Databasechangelog.findByTag", query = "SELECT d FROM Databasechangelog d WHERE d.tag = :tag"),
    @NamedQuery(name = "Databasechangelog.findByLiquibase", query = "SELECT d FROM Databasechangelog d WHERE d.liquibase = :liquibase"),
    @NamedQuery(name = "Databasechangelog.findByContexts", query = "SELECT d FROM Databasechangelog d WHERE d.contexts = :contexts"),
    @NamedQuery(name = "Databasechangelog.findByLabels", query = "SELECT d FROM Databasechangelog d WHERE d.labels = :labels"),
    @NamedQuery(name = "Databasechangelog.findByDeploymentId", query = "SELECT d FROM Databasechangelog d WHERE d.deploymentId = :deploymentId")})
public class Databasechangelog implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected DatabasechangelogPK databasechangelogPK;
    @Basic(optional = false)
    @Column(name = "DATEEXECUTED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateexecuted;
    @Basic(optional = false)
    @Column(name = "ORDEREXECUTED")
    private int orderexecuted;
    @Basic(optional = false)
    @Column(name = "EXECTYPE")
    private String exectype;
    @Column(name = "MD5SUM")
    private String md5sum;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "COMMENTS")
    private String comments;
    @Column(name = "TAG")
    private String tag;
    @Column(name = "LIQUIBASE")
    private String liquibase;
    @Column(name = "CONTEXTS")
    private String contexts;
    @Column(name = "LABELS")
    private String labels;
    @Column(name = "DEPLOYMENT_ID")
    private String deploymentId;

    public Databasechangelog() {
    }

    public Databasechangelog(DatabasechangelogPK databasechangelogPK) {
        this.databasechangelogPK = databasechangelogPK;
    }

    public Databasechangelog(DatabasechangelogPK databasechangelogPK, Date dateexecuted, int orderexecuted, String exectype) {
        this.databasechangelogPK = databasechangelogPK;
        this.dateexecuted = dateexecuted;
        this.orderexecuted = orderexecuted;
        this.exectype = exectype;
    }

    public Databasechangelog(String id, String author, String filename) {
        this.databasechangelogPK = new DatabasechangelogPK(id, author, filename);
    }

    public DatabasechangelogPK getDatabasechangelogPK() {
        return databasechangelogPK;
    }

    public void setDatabasechangelogPK(DatabasechangelogPK databasechangelogPK) {
        this.databasechangelogPK = databasechangelogPK;
    }

    public Date getDateexecuted() {
        return dateexecuted;
    }

    public void setDateexecuted(Date dateexecuted) {
        this.dateexecuted = dateexecuted;
    }

    public int getOrderexecuted() {
        return orderexecuted;
    }

    public void setOrderexecuted(int orderexecuted) {
        this.orderexecuted = orderexecuted;
    }

    public String getExectype() {
        return exectype;
    }

    public void setExectype(String exectype) {
        this.exectype = exectype;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLiquibase() {
        return liquibase;
    }

    public void setLiquibase(String liquibase) {
        this.liquibase = liquibase;
    }

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (databasechangelogPK != null ? databasechangelogPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Databasechangelog)) {
            return false;
        }
        Databasechangelog other = (Databasechangelog) object;
        if ((this.databasechangelogPK == null && other.databasechangelogPK != null) || (this.databasechangelogPK != null && !this.databasechangelogPK.equals(other.databasechangelogPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.Databasechangelog[ databasechangelogPK=" + databasechangelogPK + " ]";
    }
    
}
