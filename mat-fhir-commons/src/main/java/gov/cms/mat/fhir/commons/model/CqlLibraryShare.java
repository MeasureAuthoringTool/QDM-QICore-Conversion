/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "CQL_LIBRARY_SHARE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CqlLibraryShare.findAll", query = "SELECT c FROM CqlLibraryShare c"),
    @NamedQuery(name = "CqlLibraryShare.findByCqlLibraryShareId", query = "SELECT c FROM CqlLibraryShare c WHERE c.cqlLibraryShareId = :cqlLibraryShareId")})
public class CqlLibraryShare implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "CQL_LIBRARY_SHARE_ID")
    private String cqlLibraryShareId;
    @JoinColumn(name = "CQL_LIBRARY_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private CqlLibrary cqlLibraryId;
    @JoinColumn(name = "SHARE_LEVEL_ID", referencedColumnName = "SHARE_LEVEL_ID")
    @ManyToOne(optional = false)
    private ShareLevel shareLevelId;
    @JoinColumn(name = "CQL_LIBRARY_OWNER_USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User cqlLibraryOwnerUserId;
    @JoinColumn(name = "SHARE_USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User shareUserId;

    public CqlLibraryShare() {
    }

    public CqlLibraryShare(String cqlLibraryShareId) {
        this.cqlLibraryShareId = cqlLibraryShareId;
    }

    public String getCqlLibraryShareId() {
        return cqlLibraryShareId;
    }

    public void setCqlLibraryShareId(String cqlLibraryShareId) {
        this.cqlLibraryShareId = cqlLibraryShareId;
    }

    public CqlLibrary getCqlLibraryId() {
        return cqlLibraryId;
    }

    public void setCqlLibraryId(CqlLibrary cqlLibraryId) {
        this.cqlLibraryId = cqlLibraryId;
    }

    public ShareLevel getShareLevelId() {
        return shareLevelId;
    }

    public void setShareLevelId(ShareLevel shareLevelId) {
        this.shareLevelId = shareLevelId;
    }

    public User getCqlLibraryOwnerUserId() {
        return cqlLibraryOwnerUserId;
    }

    public void setCqlLibraryOwnerUserId(User cqlLibraryOwnerUserId) {
        this.cqlLibraryOwnerUserId = cqlLibraryOwnerUserId;
    }

    public User getShareUserId() {
        return shareUserId;
    }

    public void setShareUserId(User shareUserId) {
        this.shareUserId = shareUserId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cqlLibraryShareId != null ? cqlLibraryShareId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CqlLibraryShare)) {
            return false;
        }
        CqlLibraryShare other = (CqlLibraryShare) object;
        if ((this.cqlLibraryShareId == null && other.cqlLibraryShareId != null) || (this.cqlLibraryShareId != null && !this.cqlLibraryShareId.equals(other.cqlLibraryShareId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.CqlLibraryShare[ cqlLibraryShareId=" + cqlLibraryShareId + " ]";
    }
    
}
